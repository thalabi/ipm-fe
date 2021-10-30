package com.kerneldc.portfolio.batch;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import yahoofinance.YahooFinance;

class YahooFinanceTest {

	@Test
	void test1() throws IOException {
		var stock = YahooFinance.get("T");
		var quote = stock.getQuote();
		System.out.println(quote.getPrice());
	}
}
