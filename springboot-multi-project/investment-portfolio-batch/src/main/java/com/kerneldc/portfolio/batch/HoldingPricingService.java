package com.kerneldc.portfolio.batch;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kerneldc.common.enums.CurrencyEnum;
import com.kerneldc.portfolio.domain.Holding;
import com.kerneldc.portfolio.domain.Instrument;
import com.kerneldc.portfolio.domain.Position;
import com.kerneldc.portfolio.repository.HoldingRepository;
import com.kerneldc.portfolio.repository.PositionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingPricingService /*implements ApplicationRunner*/ {

	private final HoldingRepository holdingRepository;
	private final PositionRepository positionRepository;
	private final ExchangeRateService exchangeRateService;
	
	private record PriceAndTimestamp (BigDecimal price, LocalDateTime priceTimestamp) {};
	
	private Map<Long, PriceAndTimestamp> priceCache = new HashMap<>();
//	@Override
//    public void run(ApplicationArguments args) throws Exception {
//        LOGGER.info("Application started with option names : {}", args.getOptionNames());
//
//        priceHoldings();
//    }

	public int priceHoldings() throws IOException, InterruptedException {
        var snapshotDatetime = LocalDateTime.now();

        getAndPersistExchangeRates(snapshotDatetime.toLocalDate());

        var holdingList = holdingRepository.findLatestAsOfDateHoldings();
        priceCache.clear();
        for (Holding holding : holdingList) {
        	LOGGER.info("holding: {}", holding);
        	getAndPersistStockPrice(snapshotDatetime, holding);
        }
        return holdingList.size();
	}
	
	private void getAndPersistExchangeRates(LocalDate date) throws IOException, InterruptedException {
		var fromCurrency = CurrencyEnum.USD;
		var toCurrency = CurrencyEnum.CAD;
		exchangeRateService.retrieveAndPersistExchangeRate(date, fromCurrency, toCurrency);
	}

	private void getAndPersistStockPrice(LocalDateTime snapshotDatetime, Holding holding) throws IOException {
		var instrument = holding.getInstrument();
		var priceAndTimestamp = getPrice(instrument);
		
		var p = new Position();
		p.setPositionSnapshot(snapshotDatetime);
		p.setInstrument(instrument);
		p.setPortfolio(holding.getPortfolio());
		p.setQuantity(holding.getQuantity());
		p.setPrice(priceAndTimestamp.price);
		p.setPriceTimestamp(priceAndTimestamp.priceTimestamp);
		positionRepository.save(p);
	}
	
	private PriceAndTimestamp getPrice(Instrument instrument) throws IOException {
		var priceAndTimestamp = priceCache.get(instrument.getId());
		var ticker = instrument.getTicker();
		var exchange = instrument.getExchange();
		if (priceAndTimestamp == null) {
			Stock stock;
			// For instruments in the TSE and CNSX exchanges try appending .TO and then .CN to the symbol 
			if (Arrays.asList("TSE","CNSX").contains(exchange)) {
				ticker = ticker.replace(".", "-");
				stock = YahooFinance.get(ticker + ".TO");
				if (stock == null) {
					stock = YahooFinance.get(ticker + ".CN");
				}
			} else {
				stock = YahooFinance.get(ticker);
			}
	
			var quote =  stock.getQuote();
			var price = quote.getPrice();
			var priceTimestamp = toLocalDateTime(quote.getLastTradeTime() != null ?  quote.getLastTradeTime().getTime() : null);
			LOGGER.info("Retrieved price for {} {}: {} {}", ticker, instrument.getExchange(), price, priceTimestamp);
			priceAndTimestamp = new PriceAndTimestamp(price, priceTimestamp);
			priceCache.put(instrument.getId(), priceAndTimestamp);
		} else {
			LOGGER.info("Found {} {} in price cache", ticker, exchange);
		}
		return priceAndTimestamp;
	}
	
	private LocalDateTime toLocalDateTime(Date dateToConvert) {
		if (dateToConvert == null) {
			return null;
		}
		return LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault());
	}

}
