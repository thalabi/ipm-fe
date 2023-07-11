	package com.kerneldc.ipm.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import com.google.common.base.Preconditions;

public class TimeUtils {

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());

	private TimeUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static OffsetDateTime toOffsetDateTime(Instant instant) {
		return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
	
	public static OffsetDateTime toOffsetDateTime(Date date) {
		return date == null ? null : toOffsetDateTime(date.toInstant());
	}

	public static OffsetDateTime toOffsetDateTime(LocalDate localDate) {
		return localDate == null ? null : toOffsetDateTime(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public static OffsetDateTime toOffsetDateTime(Calendar calendar) {
		return calendar == null ? null : toOffsetDateTime(calendar.toInstant());
	}
	public static LocalDate toLocalDate(Instant instant) {
		return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.systemDefault());
	}

	public static LocalDateTime toLocalDateTime(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	public static Instant toInstant(LocalDate localDate) {
		return localDate == null ? null : localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

	public static Date toDate(LocalDateTime localDateTime) {
		return localDateTime == null ? null :  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static int compareDatePart(OffsetDateTime date1, OffsetDateTime date2) {
		Preconditions.checkArgument(date1 != null, "date1 must not be null");
		Preconditions.checkArgument(date2 != null, "date2 must not be null");
		var date1Date = date1.truncatedTo(ChronoUnit.DAYS);
		var date2Date = date2.truncatedTo(ChronoUnit.DAYS);
		return date1Date.compareTo(date2Date);
	}
}
