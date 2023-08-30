package com.kerneldc.ipm.repository;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;

public interface IPortfolioWithDependentFlags {
	Long getId();
	Integer getFinancialInstitutionNumber();
	default String getFinancialInstitution() { // set to FinancialInstitutionEnum name
		return FinancialInstitutionEnum.financialInstitutionEnumOf(getFinancialInstitutionNumber()).name();
	}
	String getName();
	String getAccountNumber();
	CurrencyEnum getCurrency();
	Boolean  getLogicallyDeleted();
	Boolean getHasHoldings();
	Boolean getHasPositions();
	Long getVersion();
}
