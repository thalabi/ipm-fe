package com.kerneldc.common.domain;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class UtcFormattedDateTest {

	@Test
	void test1() {
		var d = OffsetDateTime.now();
		var fd = d.format(AbstractEntity.OFFSET_DATE_TIME_FORMATTER.withZone(ZoneOffset.UTC));
		System.out.println(fd);
	}
}
