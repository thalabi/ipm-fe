package com.kerneldc.common.enums;

public enum ShortMonthEnum {
	JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC;

	public static Integer getNumericValue(ShortMonthEnum month) {
		if (month == null) {
			return null;
		}
		return month.ordinal()+1;
	}
}
