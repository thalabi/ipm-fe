package com.kerneldc.ipm.util;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.junit.jupiter.api.Test;

class AppTimeUtilsTest {

//	@Test
//	void testCompareDatePart_returnLess() { // first date's day is less than second date's day
//		var dateFormatter1 = DateTimeFormatter.ofPattern("uuuu-MM-dd");
//		var ldt = LocalDateTime.of(LocalDate.parse("2023-06-11", dateFormatter1), LocalTime.MIDNIGHT);
//		var yesterday = ldt.atOffset(ZoneId.systemDefault().getRules().getOffset(ldt));
//		var now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
//		System.out.println("now date part: " + dateFormatter1.format(now));
//		System.out.println("yesterday: " + dateFormatter1.format(yesterday));
//		
//		int result = AppTimeUtils.compareDatePart(yesterday, now);
//		System.out.println("compare result: " + result);
//		assertThat(AppTimeUtils.compareDatePart(yesterday, now), lessThan(0));
//	}
//
//	@Test
//	void testCompareDatePart_returnZero() { // the two dates are on the same date
//		var dateFormatter1 = DateTimeFormatter.ofPattern("uuuu-MM-dd");
//		var dateFormatter2 = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
//		var ldt1 = LocalDateTime.of(LocalDate.parse("2023-07-10", dateFormatter1), LocalTime.MIDNIGHT);
//		var ldt2 = LocalDateTime.parse("2023-07-10 23:59:59", dateFormatter2);
//		var date1 = ldt1.atOffset(ZoneId.systemDefault().getRules().getOffset(ldt1));
//		var date2 = ldt2.atOffset(ZoneId.systemDefault().getRules().getOffset(ldt2));
//		System.out.println("date1: " + dateFormatter1.format(date1));
//		System.out.println("date2: " + dateFormatter1.format(date2));
//		
//		int result = AppTimeUtils.compareDatePart(date1, date2);
//		System.out.println("compare result: " + result);
//		assertThat(AppTimeUtils.compareDatePart(date1, date2), is(0));
//	}

	@Test
	void testDifferenceIsMoreThanOne_true() {
		var dateFormatter1 = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		var odt1 = OffsetDateTime.parse("2024-09-04T16:00-04:00", dateFormatter1);
		var odt2 = OffsetDateTime.parse("2024-09-05T20:01Z", dateFormatter1);
		assertThat(AppTimeUtils.differenceIsMoreThanOneDay(odt1, odt2), is(true));
	}
	@Test
	void testDifferenceIsMoreThanOneDay_false() {
		var dateFormatter1 = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		var odt1 = OffsetDateTime.parse("2024-09-04T16:00-04:00", dateFormatter1);
		var odt2 = OffsetDateTime.parse("2024-09-04T20:00Z", dateFormatter1);
		assertThat(AppTimeUtils.differenceIsMoreThanOneDay(odt1, odt2), is(false));
	}
	@Test
	void testDateTimeFormatter_ofLocalizedDateTime() {
		var dateFormatter1 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withZone(ZoneId.systemDefault());
		var dateFormatter2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.systemDefault());
		var dateFormatter3 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());
		var dateFormatter4 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
		var now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		System.out.println("FormatStyle.FULL: " + now.format(dateFormatter1));
		System.out.println("FormatStyle.LONG: " + now.format(dateFormatter2));
		System.out.println("FormatStyle.MEDIUM: " + now.format(dateFormatter3));
		System.out.println("FormatStyle.SHORT: " + now.format(dateFormatter4));
	}

	@Test
	void testOffsetDateTimeFromString_withDateStringAndDateFormetter() {
		var result = AppTimeUtils.offsetDateTimeFromDateString("2023-08-04", DateTimeFormatter.ofPattern("uuuu-MM-dd"));
		System.out.println("result: "+ result);
	}
	@Test
	void testDaysBetween_success() {
		var dateFormatter1 = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
		var dateFormatter2 = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		var ldt1 = LocalDateTime.parse("2023-07-24 23:59:59", dateFormatter1);
		var ldt2 = LocalDateTime.of(LocalDate.parse("2023-07-31", dateFormatter2), LocalTime.MIDNIGHT);
		var date1 = ldt1.atOffset(ZoneId.systemDefault().getRules().getOffset(ldt1));
		var date2 = ldt2.atOffset(ZoneId.systemDefault().getRules().getOffset(ldt2));
		System.out.println("date1: "+date1+", date2: "+date2);
		var result = AppTimeUtils.daysBetween(date1, date2);
		System.out.println(result);
		assertThat(result, equalTo(7l));
	}
	
	@Test
	void testDefaultTimeZone() {
		ZoneId systemDefault = ZoneId.systemDefault();
		System.out.println(systemDefault);
	}
}
