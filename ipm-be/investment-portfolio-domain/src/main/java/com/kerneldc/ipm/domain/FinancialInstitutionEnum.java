package com.kerneldc.ipm.domain;

public enum FinancialInstitutionEnum {

	// For a list of all FI see https://en.wikipedia.org/wiki/Routing_number_(Canada)
	// and https://www.bankrouting.ca/financial-institution-numbers
	//
	// *** remember to update instrument_due_v as well 
	//
	BMO(1, 0),
	TD(4, 0),
	CIBC(10, 0),
	OAKEN_HOME_BANK(361, 0),
	PEOPLES_TRUST(383, 0),
	TANGERINE(614, 0),
	EQ_BANK(623, 0),
	OAKEN_HOME_TRUST(627, 0),
	DUCA(828, 21962), // transit number 21962
	SAVEN(828, 65012), //transit number 65012
	;
	
	private int institutionNumber;
	private int transitNumber;
	
	private FinancialInstitutionEnum(int institutionNumber, int transitNumber) {
		this.institutionNumber = institutionNumber;
		this.transitNumber = transitNumber; 
	}

	public int getInstitutionNumber() {
		return institutionNumber;
	}
	public int getTransitNumber() {
		return transitNumber;
	}
	
	public static FinancialInstitutionEnum financialInstitutionEnumOf(int institutionNumberColumn) {
		for (FinancialInstitutionEnum financialInstitutionEnum : values()) {
			if (financialInstitutionEnum.institutionNumber == institutionNumberColumn / 100000
					&& financialInstitutionEnum.transitNumber == institutionNumberColumn % 100000) {
				return financialInstitutionEnum;
			}
		}
		throw new IllegalArgumentException(String.format("No financial institution found with institution number %n", institutionNumberColumn));
	}
}
