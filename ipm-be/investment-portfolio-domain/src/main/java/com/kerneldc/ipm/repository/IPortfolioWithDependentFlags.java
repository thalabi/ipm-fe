package com.kerneldc.ipm.repository;

import com.kerneldc.ipm.domain.CurrencyEnum;

public interface IPortfolioWithDependentFlags {
	Long getId();
	String getLk();
	Long getVersion();
	String getInstitution();
	String getAccountNumber();
	String getName();
	CurrencyEnum getCurrency();
	Boolean getHasHoldings();
	Boolean getHasPositions();
}
