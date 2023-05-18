package com.kerneldc.ipm.rest;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import yahoofinance.YahooFinance;

class YahooFinanceApiTest {

	@Disabled
	@Test
	void testStockPrice() throws IOException {
		var stock = YahooFinance.get("BCE" + ".TO");
		System.out.println(stock.getQuote().getPrice());
	}

}
