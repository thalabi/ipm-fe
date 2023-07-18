package com.kerneldc.ipm.domain;

public enum FinancialInstitutionEnum {

	// For a list of all FI see https://en.wikipedia.org/wiki/Routing_number_(Canada)
	BMO(1),
	TD(4),
	CIBC(10),
	TANGERINE(614),
	EQ_BANK(623),
	DUCA(828)
	;
	
	private int institutionNumber;
	
	private FinancialInstitutionEnum(int institutionNumber) {
		this.institutionNumber = institutionNumber;
	}

	public int getInstitutionNumber() {
		return institutionNumber;
	}
	
	public static FinancialInstitutionEnum financialInstitutionEnumOf(int institutionNumber) {
		for (FinancialInstitutionEnum financialInstitutionEnum : values()) {
			if (financialInstitutionEnum.institutionNumber == institutionNumber) {
				return financialInstitutionEnum;
			}
		}
		throw new IllegalArgumentException(String.format("No financial institution found with institution number %n", institutionNumber));
	}
}
