package com.kerneldc.ipm.batch;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import yahoofinance.YahooFinance;

class YahooFinanceTest {

	@Disabled("Disabled until Yahoo Finance service is up.")
	@Test
	void test1() throws IOException {
		var stock = YahooFinance.get("T");
		var quote = stock.getQuote();
		System.out.println(quote.getPrice());
	}
}
