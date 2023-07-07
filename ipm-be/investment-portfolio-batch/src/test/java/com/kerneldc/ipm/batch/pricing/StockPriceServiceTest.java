package com.kerneldc.ipm.batch.pricing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.pricing.StockAndEtfPriceService;
import com.kerneldc.ipm.batch.pricing.BaseAbstractPriceService.PriceQuote;
import com.kerneldc.ipm.domain.Instrument;

class StockPriceServiceTest {

	private static StockAndEtfPriceService stockAndEtfPriceService;

	@BeforeAll
	public static void beforeAll() {
		//
		// Make user ALPHAVANTAGE_API_KEY is defined in Eclipse and Jenkins
		//
		String apiKey = System.getenv("ALPHAVANTAGE_API_KEY");
		System.out.println("Using ALPHAVANTAGE_API_KEY: " + apiKey);
		stockAndEtfPriceService = new StockAndEtfPriceService(null, apiKey);
	}

	@Test
	void TestAlphaVantageQuoteOnce() throws ApplicationException {
		var instrument = new Instrument();
		instrument.setTicker("BCE");
		instrument.setExchange("TSE");
		var priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	@Test
	void TestAlphaVantageQuoteMultipleTimes() throws ApplicationException {
		var instrument = new Instrument();

		PriceQuote priceQuote; 
		instrument.setTicker("SENS");
		instrument.setExchange("CNSX");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setExchange("TSE");
		instrument.setTicker("BBD.B");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("RY");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("AGF.B");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("SU");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("T");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	
}
