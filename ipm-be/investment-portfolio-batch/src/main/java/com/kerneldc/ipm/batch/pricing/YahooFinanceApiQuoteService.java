package com.kerneldc.ipm.batch.pricing;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.pricing.ITradingInstrumentPricingService.PriceQuote;
import com.kerneldc.ipm.batch.quote.YahooApiQuote;
import com.kerneldc.ipm.commonservices.util.HttpUtil;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class YahooFinanceApiQuoteService implements IMarketPriceQuoteService {

	private final HttpUtil httpUtil;

	public YahooFinanceApiQuoteService(HttpUtil httpUtil) {
		this.httpUtil = httpUtil;
	}

	@Override
	public PriceQuote quote(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException {
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
					urlContent = httpUtil.yahooFinanceApiContent(ticker + ".TO");
				} catch (ApplicationException exception) {
					urlContent = httpUtil.yahooFinanceApiContent(ticker + ".CN");
				}
			} else {
				urlContent = httpUtil.yahooFinanceApiContent(ticker);
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
	public int servicePriority() {
		return 3;
	}
}
