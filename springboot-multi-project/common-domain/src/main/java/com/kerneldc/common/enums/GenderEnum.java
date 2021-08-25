package com.kerneldc.common.enums;

public enum GenderEnum {
	M("Male"),
	F("Female");
	
	private String gender;
	
	private GenderEnum(String gender) {
		this.gender = gender;
	}
	
	public String getSex() {
		return gender;
	}
}
