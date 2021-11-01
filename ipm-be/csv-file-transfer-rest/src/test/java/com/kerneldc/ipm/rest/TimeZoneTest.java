package com.kerneldc.ipm.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TimeZoneTest {

	@Test
	void test1() {
		LOGGER.info("test1()");
		var d = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
		var d2 = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
		LOGGER.info("d: {}", d);
		LOGGER.info("d2: {}", d2);
		assertThat(d).isEqualTo(d2);
	}

	@Test
	void test2() {
		LOGGER.info("test2()");
		var d = LocalDate.now();
		var d2 = OffsetDateTime.of(d, LocalTime.MIDNIGHT, ZoneOffset.UTC);
		LOGGER.info("d: {}", d);
		LOGGER.info("d2: {}", d2);
		// assertThat(d).isEqualTo(d2);
	}

	@Test
	void test3() {
		LOGGER.info("test2()");
		LOGGER.info("OffsetDateTime.now().getOffset(): {}", OffsetDateTime.now().getOffset());
		LOGGER.info("ZoneId.systemDefault(): {}", ZoneId.systemDefault());
		LOGGER.info("Instant.now(): {}", Instant.now());
		var odt = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LOGGER.info("odt: {}", odt);
	}
}
