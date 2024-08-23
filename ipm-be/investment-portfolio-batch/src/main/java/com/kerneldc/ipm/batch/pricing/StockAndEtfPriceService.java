package com.kerneldc.ipm.batch.pricing;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.alphavantage.AlphavantageQuote;
import com.kerneldc.ipm.batch.alphavantage.YahooApiQuote;
import com.kerneldc.ipm.commonservices.util.UrlContentUtil;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Service
@Slf4j
public class StockAndEtfPriceService implements ITradingInstrumentPricingService<IListedInstrumentDetail> {

	private enum STOCK_PRICE_SERVICE { YAHOO_FINANCE, ALPAVANTAGE, YAHOO_FINANCE_API };
	//private static final STOCK_PRICE_SERVICE ENABLED_STOCK_PRICE_SERVICE = STOCK_PRICE_SERVICE.ALPAVANTAGE;
	private static final STOCK_PRICE_SERVICE ENABLED_STOCK_PRICE_SERVICE = STOCK_PRICE_SERVICE.YAHOO_FINANCE_API;
//	private static final String ALPHAVANTAGE_URL_TEMPLATE = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&apikey=%s&symbol=%s";
//	private static final String ALPHAVANTAGE_TEST_URL_TEMPLATE = "http://localhost:8000/%s.html";
	
	private final String alphavantageApiKey;
	private final String alphavantageApiUrlTemplate;
	private final PriceRepository priceRepository;
	private final UrlContentUtil urlContentUtil;
	
	protected Table<String, ExchangeEnum, Float> fallbackPriceLookupTable = HashBasedTable.create();
	private Map<Long, Price> latestPriceByInstrumentId;
	public StockAndEtfPriceService(PriceRepository priceRepository, UrlContentUtil urlContentUtil,
			@Value("${alphavantage.api.url.template}") String alphavantageApiUrlTemplate,
			@Value("${alphavantage.api.key}") String alphavantageApiKey) {
		
		LOGGER.info("alphavantageApiUrlTemplate: {}", alphavantageApiUrlTemplate);
		LOGGER.info("alphavantageApiKey: {}", alphavantageApiKey);
		
		this.priceRepository = priceRepository;
		this.urlContentUtil = urlContentUtil;
		this.alphavantageApiKey = alphavantageApiKey;
		this.alphavantageApiUrlTemplate = alphavantageApiUrlTemplate;
		
		fallbackPriceLookupTable.put("SENS", ExchangeEnum.CNSX, 0.01f);
		fallbackPriceLookupTable.put("BHCC", ExchangeEnum.CNSX, 0.025f);
		
		latestPriceByInstrumentId = priceRepository.findLatestPriceList().stream()
				.collect(Collectors.toMap(p -> p.getInstrument().getId(), Function.identity()));
	}
	@Override
	public PriceQuote quote(Instrument instrument, IListedInstrumentDetail instrumentStock) {
		try {
			switch (ENABLED_STOCK_PRICE_SERVICE) {
			case YAHOO_FINANCE -> {
				return yahooFinanceQuoteService(instrument, instrumentStock);
			}
			case ALPAVANTAGE -> {
				return alphaVantageQuoteService(instrument, instrumentStock);
			}
			case YAHOO_FINANCE_API -> {
				return yahooFinanceApiQuoteService(instrument, instrumentStock);
			}
			}
//			if (ENABLED_STOCK_PRICE_SERVICE == STOCK_PRICE_SERVICE.YAHOO_FINANCE) {
//				return yahooFinanceQuoteService(instrument, instrumentStock);
//			} else {
//				return alphaVantageQuoteService(instrument, instrumentStock);
//			}
			return null;
		} catch (ApplicationException e) {
			LOGGER.warn(e.getMessage());
			return fallBackToLatestAvailablePrice(instrument);
		}
	}

	private PriceQuote fallBackToLatestAvailablePrice(Instrument instrument) {
		LOGGER.warn("Attempting to set price of instrument [{}] to the latest price available.", instrument);
		var latestPrice = latestPriceByInstrumentId.get(instrument.getId());
		if (latestPrice == null) {
			LOGGER.error(
					"Could not find instrument id [{}] in list of latest instrument ids [{}] with latest price. Setting price to zero.",
					instrument.getId(), latestPriceByInstrumentId.keySet());
			return new PriceQuote(BigDecimal.ZERO, null);
		}
		LOGGER.warn("Found price of [{}] with price timestamp of [{}].", latestPrice.getPrice(), latestPrice.getPriceTimestamp());
		return new PriceQuote(latestPrice.getPrice(), latestPrice.getPriceTimestamp());
	}

	private PriceQuote yahooFinanceQuoteService(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
		var ticker = instrument.getTicker();
		var exchange = instrumentStock.getExchange();
		Stock stock;
		// For instruments in the TSE and CNSX exchanges try appending .TO and then .CN to the symbol 
		try {
			if (Arrays.asList("TSE","CNSX").contains(exchange.name())) {
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
		String urlString;
		if (StringUtils.isEmpty(alphavantageApiKey)) {
			// if alphavantageApiKey is empty then this is called during a JUnit test and we are testing against Mongoose server
			urlString = String.format(alphavantageApiUrlTemplate, alphavantageSymbol);
		} else {
			urlString = String.format(alphavantageApiUrlTemplate, alphavantageApiKey, alphavantageSymbol);
		}
		
		AlphavantageQuote quote = null;
		try {
			//var url = new URL(urlString);
			
			for (int tries = 1; tries <= MAX_RETRIES; tries++) {
				var objectMapper = new ObjectMapper();
				objectMapper.findAndRegisterModules(); // auto-discover jackson-datatype-jsr310 that handles Java 8 new date API
				
				var urlContent = urlContentUtil.getUrlContent(urlString);
				//var jsonNode = objectMapper.readTree(url);
				var jsonNode = objectMapper.readTree(urlContent);
				
				var globalQuoteJson = jsonNode.get("Global Quote");
				if (globalQuoteJson != null && globalQuoteJson.isContainerNode()) { 
					objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
					quote = objectMapper.treeToValue(jsonNode, AlphavantageQuote.class);
					//System.out.println(quote);
					break; // success, don't retry
				} else {
					if (tries == MAX_RETRIES) {
						throw new ApplicationException(String.format("Requesting quote from Alpha Vantage using url [%s] returned [%s]", urlString, jsonNode));
					}
					var noteJson = jsonNode.get("Note"); 
					if (noteJson != null && noteJson.isValueNode()) {
						var noteText = noteJson.textValue();
						var wait = noteText.contains("call frequency");
						if (wait) {
							LOGGER.info("Sleeping one minute before requesting qoute from Alpha Vantage ...");
							Thread.sleep(60*1000L);
							continue;
						}
					}
				}
				LOGGER.error("Alpha Vantage returned: [{}]", jsonNode);
				throw new ApplicationException(String.format("Requesting quote from Alpha Vantage using url [%s] returned [%s]", urlString, jsonNode));
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
			var fallbackPrice = fallbackPriceLookupTable.get(ticker, exchange);
			if (fallbackPrice != null) {
				LOGGER.warn("Unable to get quote for ticker: {} and exchange: {}, using fallback table to set price", ticker, exchange);
				return new PriceQuote(new BigDecimal(Float.toString(fallbackPrice)), OffsetDateTime.now());
			} else {
				var message = String.format("Unable to get quote for ticker: %s and exchange: %s", ticker, exchange);
				LOGGER.warn(message);
				throw new ApplicationException(message);
			}
		} else {
			return new PriceQuote(new BigDecimal(Float.toString(quote.getPrice())),
					AppTimeUtils.toOffsetDateTime(quote.getLatestTradingDay()));
		}
	}
	private PriceQuote yahooFinanceApiQuoteService(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
		var ticker = instrument.getTicker();
		var exchange = instrumentStock.getExchange();
		// For instruments in the TSE and CNSX exchanges try appending .TO and then .CN
		// to the symbol
		YahooApiQuote yahooApiQuote;
		try {
			String urlContent;
			if (Arrays.asList("TSE", "CNSX").contains(exchange.name())) {
				ticker = ticker.replace(".", "-");
				try {
					urlContent = urlContentUtil.yahooFinanceApiContent(ticker + ".TO");
				} catch (ApplicationException exception) {
					urlContent = urlContentUtil.yahooFinanceApiContent(ticker + ".CN");
				}
			} else {
				urlContent = urlContentUtil.yahooFinanceApiContent(ticker);
			}

			if (urlContent == null) {
				var message = String.format("Unable to get quote for ticker: %s and exchange: %s", ticker, exchange);
				LOGGER.warn(message);
				throw new ApplicationException(message);
			}
			var objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
			yahooApiQuote = objectMapper.readValue(urlContent, YahooApiQuote.class);
		} catch (IOException e) {
			var message = String.format("Exception while getting price quote for stock %s from Yahoo api", ticker);
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
		}

		return new PriceQuote(new BigDecimal(Float.toString(yahooApiQuote.getPrice())), yahooApiQuote.getPriceTimestamp());
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
