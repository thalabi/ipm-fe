package com.kerneldc.ipm.batch.quote;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.Data;

@Data
@JsonRootName(value = "chart")
public class YahooApiQuote {

	private Result[] result;
	private String error;

	@Data
	public static class Result {
		private Meta meta;
		private Object timestamp;
		private Object indicators;
	}
	@Data
	public static class Meta {
	    private String currency;
	    private String symbol;
	    private String exchangeName;
	    private String fullExchangeName;
	    private String instrumentType;
	    private String firstTradeDate;
	    private Long regularMarketTime;
	    private String hasPrePostMarketData;
	    private String gmtoffset;
	    private String timezone;
	    private String exchangeTimezoneName;
	    private Float regularMarketPrice;
	    private String fiftyTwoWeekHigh;
	    private String fiftyTwoWeekLow;
	    private String regularMarketDayHigh;
	    private String regularMarketDayLow;
	    private String regularMarketVolume;
	    private String longName;
	    private String shortName;
	    private String chartPreviousClose;
	    private String priceHint;
	
	    private Object currentTradingPeriod;
	    
	    private String dataGranularity;
	    private String range;
	    
		private Object validRanges;
	}

	private Meta getMeta() {
		return (getResult() != null && getResult().length != 0 && getResult()[0].getMeta() != null
				? getResult()[0].getMeta()
				: null);
	}
	
	//
	// Utility methods
	//
	public OffsetDateTime getPriceTimestamp() {
		return AppTimeUtils.toOffsetDateTime(getMeta().getRegularMarketTime());
	}
	public Float getPrice() {
		return getMeta().getRegularMarketPrice();
	}
}