package com.kerneldc.ipm.repository;

import java.math.BigDecimal;

import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.TermEnum;

public interface IHoldingDetail {
	Long getId();
	// This was changed from type OffsetDateTime, as a workaround to 'org.hibernate.MappingException: No Dialect mapping for JDBC type: 2014' exception
	String getAsOfDate();
	Long getInstrumentId();
	String getTicker();
	//String getExchange();
	String getCurrency();
	String getName();
	Float getQuantity();
	BigDecimal getLatestPrice();
	// This was changed from type OffsetDateTime, as a workaround to 'org.hibernate.MappingException: No Dialect mapping for JDBC type: 2014' exception
	String getLatestPriceTimestamp();
	
	String getInstrumentType();
	// Interest bearing instrument details
	// TODO, this should be of type FinancialInstitutionEnum but would get exception becuase jackson cannot convert
	String getFinancialInstitution();
	InterestBearingTypeEnum getType();
	TermEnum getTerm();
	Float getInterestRate();
	String getMaturityDate();
	Float getPromotionalInterestRate();
	String getPromotionEndDate();

	Long getVersion();
}
