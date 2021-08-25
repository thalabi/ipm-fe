package com.kerneldc.common.enums;

public enum ShortMonthEnum {
	JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC;

	public int getValue() {
		return this.ordinal()+1;
	}
}
