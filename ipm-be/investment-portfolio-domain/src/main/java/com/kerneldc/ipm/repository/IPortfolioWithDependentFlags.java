package com.kerneldc.ipm.repository;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.HolderEnum;

public interface IPortfolioWithDependentFlags {
	Long getId();
	Integer getFinancialInstitutionNumber();
	default String getFinancialInstitution() { // set to FinancialInstitutionEnum name
		return FinancialInstitutionEnum.financialInstitutionEnumOf(getFinancialInstitutionNumber()).name();
	}
	String getName();
	HolderEnum getHolder();
	default String getHolderName() {
		return getHolder() != null ? getHolder().getName() : StringUtils.EMPTY;
	}
	String getAccountId();
	CurrencyEnum getCurrency();
	Boolean  getLogicallyDeleted();
	Boolean getHasHoldings();
	Boolean getHasPositions();
	Long getVersion();
}
