package com.kerneldc.ipm.domain;

import lombok.Getter;

public enum HolderEnum {
	TARIF("Tarif", "LIGHT_CORNFLOWER_BLUE"),
	MAY("May", "CORAL"),
	JOINT("Joint", "YELLOW"),
	KDC("KDC", "MAROON");
	
	@Getter
	String name;
	@Getter
	String reportCellColor; 

	HolderEnum(String name, String reportCellColor) {
		this.name = name;
		this.reportCellColor = reportCellColor;
	}
}
