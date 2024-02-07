package com.kerneldc.ipm.batch.pricing;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.alphavantage.GlobalQuote;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Service
@Slf4j
public class StockAndEtfPriceService implements ITradingInstrumentPricingService<IListedInstrumentDetail> {

	private enum STOCK_PRICE_SERVICE { YAHOO, ALPAVANTAGE };
	private static final STOCK_PRICE_SERVICE ENABLED_STOCK_PRICE_SERVICE = STOCK_PRICE_SERVICE.ALPAVANTAGE;
	private static final String ALPHAVANTAGE_URL_TEMPLATE =  "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&apikey=%s&symbol=%s";
	
	private final String alphavantageApiKey;
	private final PriceRepository priceRepository;
	
	private Table<String, String, Float> fallbackPriceLookupTable = HashBasedTable.create();
	
	public StockAndEtfPriceService(PriceRepository priceRepository, @Value("${alphavantage.api.key}") String alphavantageApiKey) {
		this.priceRepository = priceRepository;
		this.alphavantageApiKey = alphavantageApiKey;
		
		fallbackPriceLookupTable.put("SENS", "CNSX", 0.01f);
		fallbackPriceLookupTable.put("BHCC", "CNSX", 0.025f);
	}

	@Override
	public PriceQuote quote(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
		if (ENABLED_STOCK_PRICE_SERVICE == STOCK_PRICE_SERVICE.YAHOO) {
			return yahooFinanceQuoteService(instrument, instrumentStock);
		} else {
			return alphaVantageQuoteService(instrument, instrumentStock);
		}
	}

	private PriceQuote yahooFinanceQuoteService(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
		var ticker = instrument.getTicker();
		var exchange = instrumentStock.getExchange();
		Stock stock;
		// For instruments in the TSE and CNSX exchanges try appending .TO and then .CN to the symbol 
		try {
			if (Arrays.asList("TSE","CNSX").contains(exchange)) {
				ticker = ticker.replace(".", "-");
				stock = YahooFinance.get(ticker + ".TO");
				if (stock == null) {
					stock = YahooFinance.get(ticker + ".CN");
				}
			} else {
				stock = YahooFinance.get(ticker);
			}
		} catch (IOException e) {
			var message = String.format("Exception while getting price quote for stock %s from Yahoo Service", ticker); 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
		}

		if (stock == null) {
			var message = String.format("Unable to get quote for ticker: %s and exchange: %s", ticker, exchange);
			LOGGER.warn(message);
			throw new ApplicationException(message);
		}
		var quote =  stock.getQuote();
		
		return new PriceQuote(quote.getPrice(), AppTimeUtils.toOffsetDateTime(quote.getLastTradeTime()));
	}
	
	private static final int MAX_RETRIES = 2;
	protected PriceQuote alphaVantageQuoteService(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
		var ticker = instrument.getTicker();
		var exchange = instrumentStock.getExchange();
		
		var alphavantageSymbol = ticker;
		if (Arrays.asList("TSE","CNSX").contains(exchange.toString())) {
			alphavantageSymbol = ticker.replace(".", "-") + ".TO";
		}
		var urlString = String.format(ALPHAVANTAGE_URL_TEMPLATE, alphavantageApiKey, alphavantageSymbol);
		
		GlobalQuote quote = null;
		try {
			var url = new URL(urlString);
			
			for (int tries = 1; tries <= MAX_RETRIES; tries++) {
				var objectMapper = new ObjectMapper();
				objectMapper.findAndRegisterModules(); // auto-discover jackson-datatype-jsr310 that handles Java 8 new date API
				
				var jsonNode = objectMapper.readTree(url);
				
				var globalQuoteJson = jsonNode.get("Global Quote");
				if (globalQuoteJson != null && globalQuoteJson.isContainerNode()) { 
					objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
					quote = objectMapper.treeToValue(jsonNode, GlobalQuote.class);
					//System.out.println(quote);
					break; // success, don't retry
				} else {
					if (tries == MAX_RETRIES) {
						throw new ApplicationException(String.format("Requesting quote from Alpha Vantage using url [%s] returned [%s]", url, jsonNode));
					}
					var noteJson = jsonNode.get("Note"); 
					if (noteJson != null && noteJson.isValueNode()) {
						var noteText = noteJson.textValue();
						var wait = noteText.contains("call frequency");
						if (wait) {
							LOGGER.info("Sleeping one minute before requesting qoute from Alpha Vantage ...");
							Thread.sleep(60*1000);
							continue;
						}
					}
				}
				LOGGER.error("Alpha Vantage returned: [{}]", jsonNode);
				throw new ApplicationException(String.format("Requesting quote from Alpha Vantage using url [%s] returned [%s]", url, jsonNode));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApplicationException(String.format("Exception requesting quote from Alpha Vantage for [%s]", urlString), e);
		} catch (InterruptedException e) {
			LOGGER.warn("Interrupted!", e);
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();

		}
		
		if (quote == null || quote.getPrice() == null) {
			var fallbackPrice = fallbackPriceLookupTable.get(instrument.getTicker(), instrumentStock.getExchange());
			if (fallbackPrice != null) {
				LOGGER.warn("Unable to get quote for ticker: {} and exchange: {}, using fallback table to set price", ticker, exchange);
				return new PriceQuote(new BigDecimal(Float.toString(fallbackPrice)), OffsetDateTime.now());
			} else {
				var message = String.format("Unable to get quote for ticker: %s and exchange: %s", ticker, exchange);
				LOGGER.warn(message);
				throw new ApplicationException(message);
			}
		} else {
			return new PriceQuote(new BigDecimal(Float.toString(quote.getPrice())), AppTimeUtils.toOffsetDateTime(quote.getLatestTradingDay()));
		}
	}
	
	@Override
	public Collection<InstrumentTypeEnum> canHandle() {
		return List.of(InstrumentTypeEnum.STOCK, InstrumentTypeEnum.ETF);
	}

	@Override
	public PriceRepository priceRepository() {
		return priceRepository;
	}
}
