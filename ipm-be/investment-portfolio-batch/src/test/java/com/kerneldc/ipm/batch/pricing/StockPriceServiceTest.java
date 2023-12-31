package com.kerneldc.ipm.batch.pricing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.pricing.ITradingInstrumentPricingService.PriceQuote;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;

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

	@Disabled // temporarily because exceeded 25 quotes per day  
	@Test
	void TestAlphaVantageQuoteOnce() throws ApplicationException {
		var instrument = new Instrument();
		var instrumentStock = new InstrumentStock();
		instrumentStock.setInstrument(instrument);
		instrument.setTicker("BCE");
		instrumentStock.setExchange(ExchangeEnum.TSE);
		var priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	@Disabled // temporarily because exceeded 25 quotes per day  
	@Test
	void TestAlphaVantageQuoteMultipleTimes() throws ApplicationException {
		var instrument = new Instrument();
		var instrumentStock = new InstrumentStock();
		instrumentStock.setInstrument(instrument);

		PriceQuote priceQuote; 
//		instrument.setTicker("SENS");
//		instrumentStock.setExchange(ExchangeEnum.CNSX);
//		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
//		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrumentStock.setExchange(ExchangeEnum.TSE);
		instrument.setTicker("BBD.B");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("RY");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("AGF.B");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("SU");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
		instrument.setTicker("T");
		priceQuote = stockAndEtfPriceService.alphaVantageQuoteService(instrument, instrumentStock);
		assertThat(priceQuote.lastPrice(), greaterThan(new BigDecimal("0")));
	}
	
}
