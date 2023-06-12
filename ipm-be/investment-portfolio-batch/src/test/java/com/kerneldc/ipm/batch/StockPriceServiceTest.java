package com.kerneldc.ipm.batch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.BaseAbstractPriceService.PriceQuote;
import com.kerneldc.ipm.domain.Instrument;

class StockPriceServiceTest {

	private static StockPriceService stockPriceService;

	@BeforeAll
	public static void beforeAll() {
		String apiKey = System.getenv("ALPHAVANTAGE_API_KEY");
		System.out.println("Using ALPHAVANTAGE_API_KEY: " + apiKey);
		stockPriceService = new StockPriceService(null, apiKey);
	}

	@Test
	void TestAlphaVantageQuoteOnce() throws ApplicationException {
		var instrument = new Instrument();
		instrument.setTicker("BCE");
		instrument.setExchange("TSE");
		var priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	@Test
	void TestAlphaVantageQuoteMultipleTimes() throws ApplicationException {
		var instrument = new Instrument();

		PriceQuote priceQuote; 
		instrument.setTicker("SENS");
		instrument.setExchange("CNSX");
		priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setExchange("TSE");
		instrument.setTicker("BBD.B");
		priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("RY");
		priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("AGF.B");
		priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("SU");
		priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("T");
		priceQuote = stockPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	
}
