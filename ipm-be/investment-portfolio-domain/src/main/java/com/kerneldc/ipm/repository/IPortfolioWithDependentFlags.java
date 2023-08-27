package com.kerneldc.ipm.repository;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;

public interface IPortfolioWithDependentFlags {
	Long getId();
	String getLk();
	Integer getFinancialInstitution();
	default String getFinancialInstitutionName() { // set to FinancialInstitutionEnum name
		return FinancialInstitutionEnum.financialInstitutionEnumOf(getFinancialInstitution()).name();
	}
	String getName();
	String getAccountNumber();
	CurrencyEnum getCurrency();
	Boolean  getLogicallyDeleted();
	Boolean getHasHoldings();
	Boolean getHasPositions();
	Long getVersion();
}
