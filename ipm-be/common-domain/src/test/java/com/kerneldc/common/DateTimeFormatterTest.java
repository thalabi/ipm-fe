package com.kerneldc.common;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

class DateTimeFormatterTest {

	@Test
	void test1() {
		var f1 = "uuuu-MM-dd HH:mm:ss.SSSZ";
		var dtp1 = DateTimeFormatter.ofPattern(f1);
		var n = OffsetDateTime.now();
		System.out.println(dtp1.format(n));
	}
}
