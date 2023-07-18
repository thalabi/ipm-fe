package com.kerneldc.ipm.domain.instrumentdetail;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentInterestBearingRepository;

@DataJpaTest
class InstrumentInterestBearingRepositoryTest {

	@Autowired
	private InstrumentInterestBearingRepository instrumentInterestBearingRepository;
	
	@Test
	void testInstrumentInterestBearingtRepositoryIsNotNull() {
		assertThat(instrumentInterestBearingRepository).isNotNull();
	}

	@Test
	void testInsertInstrumentInterestBearingtRepository() {
		var i = new Instrument();
		i.setType(InstrumentTypeEnum.INTEREST_BEARING);
		i.setTicker("CIB275");
		i.setName("Canadian Imperial Bank of Comm CIB275");
		i.setCurrency(CurrencyEnum.USD);
		var ib = new InstrumentInterestBearing();
		ib.setInstrument(i);
		ib.setType(InterestBearingTypeEnum.MONEY_MARKET);
		ib.setPrice(new BigDecimal("10"));
		ib.setInterestRate(4.1f);
		instrumentInterestBearingRepository.saveAndFlush(ib);
		System.out.println("Instrument: " + i);
		System.out.println("InstrumentInterestBearing: " + ib);
		assertThat(ib.getId()).isNotNull();
		assertThat(ib.getInstrument().getId()).isNotNull();
	}

	@Test
	void testInsertInstrumentInterestBearingtRepository2() {
		var i = new Instrument();
		i.setType(InstrumentTypeEnum.INTEREST_BEARING);
		i.setTicker("Chequing");
		i.setName("Chequing Account");
		i.setCurrency(CurrencyEnum.CAD);
		var ib = new InstrumentInterestBearing();
		ib.setInstrument(i);
		ib.setType(InterestBearingTypeEnum.CHEQUING);
		ib.setPrice(new BigDecimal("1"));
		ib.setInterestRate(1f);
		ib.setFinancialInstitution(FinancialInstitutionEnum.TANGERINE);
		instrumentInterestBearingRepository.saveAndFlush(ib);
		System.out.println("Instrument: " + i);
		System.out.println("InstrumentInterestBearing: " + ib);
		System.out.println("InstrumentInterestBearing lk: " + ib.getLogicalKeyHolder());
		assertThat(ib.getId()).isNotNull();
		assertThat(ib.getInstrument().getId()).isNotNull();
	}

}
