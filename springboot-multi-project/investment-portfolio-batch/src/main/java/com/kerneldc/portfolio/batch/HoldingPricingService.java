package com.kerneldc.portfolio.batch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.enums.CurrencyEnum;
import com.kerneldc.portfolio.domain.Holding;
import com.kerneldc.portfolio.domain.Instrument;
import com.kerneldc.portfolio.domain.Position;
import com.kerneldc.portfolio.domain.Price;
import com.kerneldc.portfolio.repository.HoldingRepository;
import com.kerneldc.portfolio.repository.PositionRepository;
import com.kerneldc.portfolio.repository.PriceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingPricingService /*implements ApplicationRunner*/ {
	
	private static final String CASH_TICKER = "CASH";

	private static final boolean OFFLINE_MODE = false;

	private final HoldingRepository holdingRepository;
	private final PositionRepository positionRepository;
	private final PriceRepository priceRepository;
	private final ExchangeRateService exchangeRateService;
	
	private LocalDateTime snapshotDatetime;
	
	//private static final Price CASH_PRICE = new Price();
	private Price CASH_CAD_PRICE;
	private Price CASH_USD_PRICE;
//	{		
//		CASH_PRICE.setPrice(BigDecimal.ONE);
//	}
	
	private Map<Long, Price> priceCache = new HashMap<>();
//	@Override
//    public void run(ApplicationArguments args) throws Exception {
//        LOGGER.info("Application started with option names : {}", args.getOptionNames());
//
//        priceHoldings();
//    }

	public int priceHoldings() throws IOException, InterruptedException {
        snapshotDatetime = LocalDateTime.now();
        
        CASH_CAD_PRICE = priceRepository.findById(-1l).orElseThrow();
        CASH_USD_PRICE = priceRepository.findById(-2l).orElseThrow();


		if (! /* not */ OFFLINE_MODE) {
			getAndPersistExchangeRates();
		}

        var holdingList = holdingRepository.findLatestAsOfDateHoldings();
        priceCache.clear();
        var positionList = new ArrayList<Position>();
        for (Holding holding : holdingList) {
        	LOGGER.info("holding: {}", holding);
        	//getAndPersistStockPrice(holding);
        	positionList.add(getStockPrice(holding));
        }
        persistPrices();
        persistPositions(positionList);
        return holdingList.size();
	}
	
	private void persistPositions(ArrayList<Position> positionList) {
		var savedPositionList = positionRepository.saveAll(positionList);
		LOGGER.info("Saved {} position records", savedPositionList.size());
	}

	private Position getStockPrice(Holding holding) throws IOException {
		var instrument = holding.getInstrument();
		var price = getPrice(instrument);

		var position = new Position();
		position.setPositionSnapshot(snapshotDatetime);
		position.setInstrument(instrument);
		position.setPortfolio(holding.getPortfolio());
		position.setQuantity(holding.getQuantity());

		position.setPriceEntity(price);

		position.setPrice(price.getPrice());
		position.setPriceTimestamp(price.getPriceTimestamp());
		return position;
	}

	private void persistPrices() {
		var priceCollection = priceCache.values();
		LOGGER.info("Price cache count: {}", priceCollection.size());
		// remove price records that already exist
		//priceCollection.removeIf(price -> ! /* not */ priceRepository.findByLogicalKeyHolder(price.getLogicalKeyHolder()).isEmpty());
			
		var savedPriceList = priceRepository.saveAll(priceCollection);
		LOGGER.info("Saved {} price records", savedPriceList.size());
	}

	private void getAndPersistExchangeRates() throws IOException, InterruptedException {
		var fromCurrency = CurrencyEnum.USD;
		var toCurrency = CurrencyEnum.CAD;
		exchangeRateService.retrieveAndPersistExchangeRate(snapshotDatetime.toLocalDate(), fromCurrency, toCurrency);
	}

	private void getAndPersistStockPrice(Holding holding) throws IOException {
		var instrument = holding.getInstrument();
		var price = getPrice(instrument);
		
		var p = new Position();
		p.setPositionSnapshot(snapshotDatetime);
		p.setInstrument(instrument);
		p.setPortfolio(holding.getPortfolio());
		p.setQuantity(holding.getQuantity());
		
		p.setPriceEntity(price);
		
		p.setPrice(price.getPrice());
		p.setPriceTimestamp(price.getPriceTimestamp());
		positionRepository.save(p);
	}
	
	private Price getPrice(Instrument instrument) throws IOException {
		
		if (OFFLINE_MODE) {
			return CASH_CAD_PRICE;
		}
		if (StringUtils.equals(instrument.getTicker(), CASH_TICKER)) {
			switch (instrument.getCurrency()) {
				case CAD:
					return CASH_CAD_PRICE;
				case USD:
					return CASH_USD_PRICE;
				default:
					throw new IllegalArgumentException("Currency should be either CAD or USD");
			}
		} else {
			return getSecurityPrice(instrument);
		}
	}
	
	private Price getSecurityPrice(Instrument instrument) throws IOException {
		var price = priceCache.get(instrument.getId());
		var ticker = instrument.getTicker();
		var exchange = instrument.getExchange();
		if (price == null) {
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
//			var price = quote.getPrice();
//			var priceTimestamp = toLocalDateTime(quote.getLastTradeTime() != null ?  quote.getLastTradeTime().getTime() : null);
//			LOGGER.info("Retrieved price for {} {}: {} {}", ticker, instrument.getExchange(), price, priceTimestamp);
//			priceAndTimestamp = new PriceAndTimestamp(price, priceTimestamp);
//			priceCache.put(instrument.getId(), priceAndTimestamp);
			
			price = new Price();
			price.setInstrument(instrument);
			if (quote.getLastTradeTime() != null) {
				price.setPriceTimestamp(toLocalDateTime(quote.getLastTradeTime().getTime()));
				price.setPriceTimestampFromSource(true);
			} else {
				price.setPriceTimestamp(snapshotDatetime);
				price.setPriceTimestampFromSource(false);
				LOGGER.warn("PriceTimestamp was not available from source for {} {}. Used current timestamp", ticker, instrument.getExchange());
			}
			var priceList = priceRepository.findByLogicalKeyHolder(price.getLogicalKeyHolder());
			if (CollectionUtils.isNotEmpty(priceList)) {
				price = priceList.get(0);
			} else {
				price.setPrice(quote.getPrice());
			}
			LOGGER.info("Retrieved price for {} {}: {} {}", ticker, instrument.getExchange(), price.getPrice(), price.getPriceTimestamp());
			priceCache.put(instrument.getId(), price);
		} else {
			LOGGER.info("Found {} {} in price cache", ticker, exchange);
		}
		return price;
	}
	
	private LocalDateTime toLocalDateTime(Date dateToConvert) {
		if (dateToConvert == null) {
			return null;
		}
		return LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault());
	}

}
