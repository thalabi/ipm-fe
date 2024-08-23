package com.kerneldc.ipm.batch.alphavantage;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

@Data
@JsonRootName(value = "Global Quote")
public class AlphavantageQuote {

	@JsonProperty("01. symbol")
	private String symbol;
	@JsonProperty("02. open")
	private Float open;
	@JsonProperty("03. high")
	private Float hight;
	@JsonProperty("04. low")
	private Float low;
	@JsonProperty("05. price")
	private Float price;
	@JsonProperty("06. volume")
	private Float volume;
	@JsonProperty("07. latest trading day")
	private LocalDate latestTradingDay;
	@JsonProperty("08. previous close")
	private Float previousClose;
	@JsonProperty("09. change")
	private Float change;
	@JsonProperty("10. change percent")
	private String changePercent; // TODO parse and convert to float
}
