package com.kerneldc.portfolio.repository;

import java.time.LocalDate;

public interface HoldingDetail {
	Long getId();
	LocalDate getAsOfDate();
	Long getInstrumentId();
	String getTicker();
	String getExchange();
	String getCurrency();
	String getName();
	Float getQuantity();
	
	Long getVersion();
}
