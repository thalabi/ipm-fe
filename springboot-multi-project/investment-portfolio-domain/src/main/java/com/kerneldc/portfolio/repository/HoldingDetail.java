package com.kerneldc.portfolio.repository;

import java.math.BigDecimal;

public interface HoldingDetail {
	Long getId();
	// This was changed from type OffsetDateTime, as a workaround to 'org.hibernate.MappingException: No Dialect mapping for JDBC type: 2014' exception
	String getAsOfDate();
	Long getInstrumentId();
	String getTicker();
	String getExchange();
	String getCurrency();
	String getName();
	Float getQuantity();
	BigDecimal getLatestPrice();
	// This was changed from type OffsetDateTime, as a workaround to 'org.hibernate.MappingException: No Dialect mapping for JDBC type: 2014' exception
	String getLatestPriceTimestamp();
	
	Long getVersion();
}
