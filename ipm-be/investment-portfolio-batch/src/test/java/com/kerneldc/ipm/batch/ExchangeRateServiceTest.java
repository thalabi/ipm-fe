package com.kerneldc.ipm.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kerneldc.ipm.commonservices.util.UrlContentUtil;
import com.kerneldc.ipm.repository.ExchangeRateRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

class ExchangeRateServiceTest {

	private ExchangeRateRepository exchangeRateRepository;
	private ExchangeRateService exchangeRateService;
	private UrlContentUtil urlContentUtil;

	@BeforeEach
	void setup() {
		urlContentUtil = new UrlContentUtil(StringUtils.EMPTY, StringUtils.EMPTY);
		exchangeRateService =  new ExchangeRateService(exchangeRateRepository, urlContentUtil);
	}
	@Test
	void testGetWorkingBusinessDay_JanuaryFirst_Success () {
		
		// Test January 1 case
		var date1 = LocalDate.of(2021,1,1);
		var expectedResult1 = LocalDate.of(2020,12,31);
		var result1 = AppTimeUtils.toLocalDate(exchangeRateService.getWorkingBusinessDay(AppTimeUtils.toInstant(date1)));
		assertThat(result1).isEqualTo(expectedResult1);
	}
	@Test
	void testGetWorkingBusinessDay_DecemberTwentyFifth_Success () {

		// Test December 25 case
		var date2 = LocalDate.of(2021,12,25);
		var expectedResult2 = LocalDate.of(2021,12,24);
		var result2 = AppTimeUtils.toLocalDate(exchangeRateService.getWorkingBusinessDay(AppTimeUtils.toInstant(date2)));
		assertThat(result2).isEqualTo(expectedResult2);
	}
	@Test
	void testGetWorkingBusinessDay_Sunday_Success () {

		// Test Sunday case
		var date3 = LocalDate.of(2021,10,10);
		var expectedResult3 = LocalDate.of(2021,10,8);
		var result3 = AppTimeUtils.toLocalDate(exchangeRateService.getWorkingBusinessDay(AppTimeUtils.toInstant(date3)));
		assertThat(result3).isEqualTo(expectedResult3);
	}

	@Test
	void testGetWorkingBusinessDay_Saturday_Success () {

		// Test Sunday case
		var date3 = LocalDate.of(2021,10,9);
		var expectedResult3 = LocalDate.of(2021,10,8);
		var result3 = AppTimeUtils.toLocalDate(exchangeRateService.getWorkingBusinessDay(AppTimeUtils.toInstant(date3)));
		assertThat(result3).isEqualTo(expectedResult3);
	}

	@Test
	void testGetWorkingBusinessDay_JanuaryFirstOnMonday_Success () {

		// Test Sunday case
		var date3 = LocalDate.of(2018,1,1);
		var expectedResult3 = LocalDate.of(2017,12,29);
		var result3 = AppTimeUtils.toLocalDate(exchangeRateService.getWorkingBusinessDay(AppTimeUtils.toInstant(date3)));
		assertThat(result3).isEqualTo(expectedResult3);
	}

}
