package com.kerneldc.ipm.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

class LibPhoneNumberTest {

	static PhoneNumberUtil phoneNumberUtil;
	
	@BeforeAll
	static void init() {
		phoneNumberUtil = PhoneNumberUtil.getInstance();
	}
	@Test
	void test1() throws NumberParseException {
		PhoneNumber pn = new PhoneNumber();
		pn.setCountryCode(1).setNationalNumber(4162005175l);
		System.out.println(pn.toString());
		assertThat(phoneNumberUtil.isValidNumber(pn)).isTrue();
	}
	@Test
	void testAreaCode() throws NumberParseException {

		//PhoneNumber number = phoneNumberUtil.parse("16502530000", "US");
		PhoneNumber number = phoneNumberUtil.parse("16502530000", "CA");
		String nationalSignificantNumber = phoneNumberUtil.getNationalSignificantNumber(number);
		String areaCode;
		String subscriberNumber;

		int areaCodeLength = phoneNumberUtil.getLengthOfGeographicalAreaCode(number);
		if (areaCodeLength > 0) {
			areaCode = nationalSignificantNumber.substring(0, areaCodeLength);
			subscriberNumber = nationalSignificantNumber.substring(areaCodeLength);
		} else {
			areaCode = "";
			subscriberNumber = nationalSignificantNumber;
		}
		System.out.println(areaCode);
		System.out.println(subscriberNumber);
		System.out.println(number.toString()+","+number.getRawInput());
	}
	
	@ParameterizedTest
	@ValueSource(longs = {4162005175l, 4165551212l})
	void testValidNumbers(long nationalNumber) {
		PhoneNumber pn = new PhoneNumber();
		pn.setCountryCode(1).setNationalNumber(nationalNumber);
		System.out.println(pn.toString());
		assertThat(phoneNumberUtil.isValidNumber(pn)).isTrue();
	}
	@ParameterizedTest
	@ValueSource(longs = {7772005175l, 416555121l})
	void testInvalidNumbers(long nationalNumber) {
		PhoneNumber pn = new PhoneNumber();
		pn.setCountryCode(1).setNationalNumber(nationalNumber);
		System.out.println(pn.toString());
		assertThat(phoneNumberUtil.isValidNumber(pn)).isFalse();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"14162005175", "14165551212", "1-416-200-5175", "416-200-5175", "1.416.200.5175", "+1.416.200.5175"})
	void testParseNumbers(String string) throws NumberParseException {
		PhoneNumber number = phoneNumberUtil.parse(string, "CA");
		assertThat(phoneNumberUtil.isValidNumber(number)).isTrue();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"17772005175", "1416555"})
	void testParseNumbersAreNotValid(String string) throws NumberParseException {
		PhoneNumber number = phoneNumberUtil.parse(string, "CA");
		assertThat(phoneNumberUtil.isValidNumber(number)).isFalse();
	}
	
}
