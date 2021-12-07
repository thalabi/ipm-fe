package com.kerneldc.common.enums;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShortMonthEnumTest {

	@Test
	void test1() {
		ShortMonthEnum month = null;
		@SuppressWarnings("null")
		Exception exception = Assertions.assertThrows(NullPointerException.class, () -> month.ordinal()); 
		assertThat(exception.getMessage(), containsString("is null"));
	}

	@Test
	void test2() {
		ShortMonthEnum month = null;
		ShortMonthEnum.getNumericValue(month);
	}

	@Test
	void test3() {
		int i = 1;
		Object o = i;
		System.out.println(o);
		System.out.println(o.getClass().getSimpleName());
	}
}
