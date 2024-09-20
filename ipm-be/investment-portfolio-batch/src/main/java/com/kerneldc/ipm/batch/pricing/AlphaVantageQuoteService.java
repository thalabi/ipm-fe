package com.kerneldc.ipm.batch.pricing;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.pricing.ITradingInstrumentPricingService.PriceQuote;
import com.kerneldc.ipm.batch.quote.AlphavantageQuote;
import com.kerneldc.ipm.commonservices.util.HttpUtil;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlphaVantageQuoteService implements IMarketPriceQuoteService {

	private static final int MAX_RETRIES = 2;

	private final HttpUtil httpUtil;
	protected Table<String, ExchangeEnum, Float> fallbackPriceLookupTable = HashBasedTable.create();


	public AlphaVantageQuoteService(HttpUtil httpUtil) {
		this.httpUtil = httpUtil;
		
		fallbackPriceLookupTable.put("SENS", ExchangeEnum.CNSX, 0.01f);
		fallbackPriceLookupTable.put("BHCC", ExchangeEnum.CNSX, 0.025f);
	}

	@Override
	public PriceQuote quote(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
		var ticker = instrument.getTicker();
		var exchange = instrumentStock.getExchange();
		
		var alphavantageSymbol = ticker;
		if (Arrays.asList("TSE","CNSX").contains(exchange.toString())) {
			alphavantageSymbol = ticker.replace(".", "-") + ".TO";
		}
		
		AlphavantageQuote quote = null;
		try {
			
			for (int tries = 1; tries <= MAX_RETRIES; tries++) {
				var objectMapper = new ObjectMapper();
				objectMapper.findAndRegisterModules(); // auto-discover jackson-datatype-jsr310 that handles Java 8 new date API
				
				var urlContent = httpUtil.alphavantageApiContent(alphavantageSymbol);
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
						throw new ApplicationException(String.format("Requesting quote from Alpha Vantage returned [%s]", jsonNode));
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
				LOGGER.warn("Alpha Vantage returned: [{}]", jsonNode);
				throw new ApplicationException(String.format("Requesting quote from Alpha Vantage returned [%s]", jsonNode));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApplicationException(String.format("Exception requesting quote from Alpha Vantage for ticker [%s]", alphavantageSymbol), e);
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

	@Override
	public int servicePriority() {
		return 2;
	}
}
