package com.kerneldc.ipm.domain;

public enum FinancialInstitutionEnum {

	// For a list of all FI see https://en.wikipedia.org/wiki/Routing_number_(Canada)
	// and https://www.bankrouting.ca/financial-institution-numbers
	//
	// *** remember to update instrument_due_v as well 
	//
	BMO(1),
	TD(4),
	CIBC(10),
	OAKEN_HOME_BANK(361),
	PEOPLES_TRUST(383),
	TANGERINE(614),
	EQ_BANK(623),
	OAKEN_HOME_TRUST(627),
	DUCA(828), // transit number 21962
	SAVEN(9999), //transit number 65012
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
