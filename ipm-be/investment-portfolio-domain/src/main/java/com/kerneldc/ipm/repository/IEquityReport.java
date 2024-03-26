package com.kerneldc.ipm.repository;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.HolderEnum;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;

public interface IEquityReport {
	Integer getFinancialInstitutionNumber();
	default String getFinancialInstitution() { // set to FinancialInstitutionEnum name
		return getFinancialInstitutionNumber() != null ? FinancialInstitutionEnum.financialInstitutionEnumOf(getFinancialInstitutionNumber()).name() : "NA";
	}
	HolderEnum getHolder();
	default String getHolderName() {
		return getHolder() != null ? getHolder().getName() : StringUtils.EMPTY;
	}
	String getPortfolioId();
	String getPortfolioName();
	CurrencyEnum getCurrency();
	String getInstrumentName();
	String getTicker();
	InstrumentTypeEnum getInstrumentType();
	BigDecimal getQuantity();
	BigDecimal getPrice();
	String getPriceTimestamp();
	Boolean getPriceTimestampFromSource();
	default BigDecimal getMarketValue() {
		return getQuantity().multiply(getPrice());
	}
}
