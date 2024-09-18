package com.kerneldc.common.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class LogicalKeyHolderTest {

	@Test
	void testOffsetDateTime() {
		var odt = OffsetDateTime.of(2024, 9, 15, 21, 42, 15, 7000000, ZoneOffset.ofHours(-4));
		var id = 36;
		var logicalKeyHolder = LogicalKeyHolder.build(id, odt);
		assertThat(logicalKeyHolder.getLogicalKey(), startsWith("36"));
		assertThat(logicalKeyHolder.getLogicalKey(), endsWith("Z"));
	}

}
