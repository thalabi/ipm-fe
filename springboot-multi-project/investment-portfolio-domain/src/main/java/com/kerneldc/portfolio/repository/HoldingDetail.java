package com.kerneldc.portfolio.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface HoldingDetail {
	Long getId();
	LocalDate getAsOfDate();
	Long getInstrumentId();
	String getTicker();
	String getExchange();
	String getCurrency();
	String getName();
	Float getQuantity();
	BigDecimal getLatestPrice();
	LocalDateTime getLatestPriceTimestamp();
	
	Long getVersion();
}
