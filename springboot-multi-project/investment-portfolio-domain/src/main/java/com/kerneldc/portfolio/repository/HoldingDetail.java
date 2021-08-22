package com.kerneldc.portfolio.repository;

public interface HoldingDetail {
	Long getInstrumentId();
	String getTicker();
	String getExchange();
	String getCurrency();
	String getName();
	Float getQuantity();
}
