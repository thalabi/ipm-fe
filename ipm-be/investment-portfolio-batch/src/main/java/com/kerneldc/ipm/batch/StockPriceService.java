package com.kerneldc.ipm.batch;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.TimeUtils;

import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Service
@Slf4j
public class StockPriceService extends BaseAbstractPriceService {

	public StockPriceService(PriceRepository priceRepository) {
		super(priceRepository);
	}

	@Override
	public PriceQuote quote(Instrument instrument) throws ApplicationException {
		return yahooFinanceQuoteService(instrument);
	}

	private PriceQuote yahooFinanceQuoteService(Instrument instrument) throws ApplicationException {
		var ticker = instrument.getTicker();
		var exchange = instrument.getExchange();
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
		
		return new PriceQuote(quote.getPrice(), TimeUtils.toOffsetDateTime(quote.getLastTradeTime()));
	}

}
