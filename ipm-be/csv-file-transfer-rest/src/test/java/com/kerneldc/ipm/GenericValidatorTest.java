package com.kerneldc.ipm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import org.apache.commons.validator.GenericValidator;
import org.junit.jupiter.api.Test;

class GenericValidatorTest {
	
	private static final String INPUT_DATE_TIME_PATTERN_1 = "M/d/y H:m";
	private static final SimpleDateFormat INPUT_SIMPLE_DATE_TIME_FORMAT_1 = new SimpleDateFormat(INPUT_DATE_TIME_PATTERN_1);
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M/d/uuuu H:mm");  

	@Test
	void testDate1() {
		var accountCreatedDateIsValid = GenericValidator.isDate("01/02/2009", "MM/dd/yyyy", false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
	}
	@Test
	void testDate2() {
		var accountCreatedDateIsValid = GenericValidator.isDate("01/02/2009 04:53", INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
	}
	@Test
	void testDate3WrongDay() {
		var accountCreatedDateIsValid = GenericValidator.isDate("99/22/2009 04:53", INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(false));
	}
	@Test
	void testDate4() {
		var dateTime = "12/31/2009 04:53";
		var accountCreatedDateIsValid = GenericValidator.isDate(dateTime, INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
		try {
			var parsedDate = INPUT_SIMPLE_DATE_TIME_FORMAT_1.parse(dateTime);
			System.out.println(parsedDate);
		} catch (ParseException e) {
			assertThat(true, equalTo(false));
		}
	}
	@Test
	void testDate5() {
		var accountCreatedDateIsValid = GenericValidator.isDate("1/31/2009 04:53", INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
	}
	@Test
	void testDate6() {
		var accountCreatedDateIsValid = GenericValidator.isDate("12/1/2009 04:53", INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
	}
	@Test
	void testDate7WrongMonth() {
		var accountCreatedDateIsValid = GenericValidator.isDate("13/1/2009 04:53", INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(false));
	}
	@Test
	void testDate8() {
		var dateTime = "1/1/2009 4:1";
		var accountCreatedDateIsValid = GenericValidator.isDate(dateTime, INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
	}
	@Test
	void testDate9WrongSeconds() {
		var dateTime = "1/1/2009 4:61";
		var accountCreatedDateIsValid = GenericValidator.isDate(dateTime, INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(false));
	}
	@Test
	void testDate10() throws ParseException {
		var dateTime = "11/15/08 15:47";
		var accountCreatedDateIsValid = GenericValidator.isDate(dateTime, INPUT_DATE_TIME_PATTERN_1, false);

		assertThat(accountCreatedDateIsValid, equalTo(true));
		try {
			var parsedDate = INPUT_SIMPLE_DATE_TIME_FORMAT_1.parse(dateTime);
			System.out.println(parsedDate);
		} catch (ParseException e) {
			assertThat(true, equalTo(false));
		}
	}

}
