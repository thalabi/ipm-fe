package com.kerneldc.portfolio.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtils {

	private TimeUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static OffsetDateTime toOffsetDateTime(Instant instant) {
		return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
	
	public static OffsetDateTime toOffsetDateTime(Date date) {
		return toOffsetDateTime(date.toInstant());
	}

	public static LocalDate toLocalDate(Instant instant) {
		return LocalDate.ofInstant(instant, ZoneId.systemDefault());
	}

	public static LocalDateTime toLocalDateTime(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
	
	public static Instant toInstant(LocalDate localDate) {
		return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

}
