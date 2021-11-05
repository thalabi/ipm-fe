package com.kerneldc.ipm.batch;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.enums.CurrencyEnum;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.HoldingPriceInterdayV;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Position;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.repository.HoldingPriceInterdayVRepository;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.EmailService;
import com.kerneldc.ipm.util.TimeUtils;

import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
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
	private final HoldingPriceInterdayVRepository holdingPriceInterdayVRepository;
	private final ExchangeRateService exchangeRateService;
	private final EmailService emailService;
	
	private Instant snapshotInstant;
	
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

	public int priceHoldings() throws IOException, InterruptedException, MessagingException, ParseException, TemplateException {
		return priceHoldings(true); 
	}
	public int priceHoldings(boolean sendNotifications) throws IOException, InterruptedException, MessagingException, ParseException, TemplateException {
        snapshotInstant = Instant.now();
        
        CASH_CAD_PRICE = priceRepository.findById(-1l).orElseThrow();
        CASH_USD_PRICE = priceRepository.findById(-2l).orElseThrow();


		if (! /* not */ OFFLINE_MODE) {
			getAndPersistExchangeRates();
		}

        var holdingList = holdingRepository.findLatestAsOfDateHoldings();
        priceCache.clear();
        var positionList = new ArrayList<Position>();
        for (Holding holding : holdingList) {
        	positionList.add(getStockPrice(holding));
        }
        persistPrices();
        persistPositions(positionList);
        if (sendNotifications) {
        	sendPriceHoldingsNotifications();
        }
        return holdingList.size();
	}
	
	private void sendPriceHoldingsNotifications() throws TemplateNotFoundException, MalformedTemplateNameException, MessagingException, ParseException, IOException, TemplateException {
		var nMarketValues =  holdingPriceInterdayVRepository.selectLastestNMarketValues(5);
		for (HoldingPriceInterdayV holdingPriceInterdayV : nMarketValues) {
			LOGGER.info("holdingPriceInterdayV: {}", holdingPriceInterdayV);
		}
		if (CollectionUtils.isEmpty(nMarketValues)) {
			LOGGER.warn("No market values available");
			return;
		}
		var todaysIndex = nMarketValues.size()-1;
		var todaysSnapshot = nMarketValues.get(todaysIndex).getPositionSnapshot();
		var todaysMarketValue = nMarketValues.get(todaysIndex).getMarketValue();
		nMarketValues.remove(todaysIndex);
		emailService.sendDailyMarketValueNotification("tarif.halabi@gmail.com", todaysSnapshot, todaysMarketValue, nMarketValues);
	}

	private void persistPositions(ArrayList<Position> positionList) {
		var savedPositionList = positionRepository.saveAll(positionList);
		LOGGER.info("Saved {} position records", savedPositionList.size());
	}

	private Position getStockPrice(Holding holding) throws IOException {
		var instrument = holding.getInstrument();
		var price = getPrice(instrument);

		var position = new Position();
		position.setPositionSnapshot(OffsetDateTime.ofInstant(snapshotInstant, ZoneId.systemDefault()));
		position.setInstrument(instrument);
		position.setPortfolio(holding.getPortfolio());
		position.setQuantity(holding.getQuantity());

		position.setPrice(price);

		//position.setPrice(price.getPrice());
		//position.setPriceTimestamp(price.getPriceTimestamp());
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
		exchangeRateService.retrieveAndPersistExchangeRate(snapshotInstant, fromCurrency, toCurrency);
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
				price.setPriceTimestamp(TimeUtils.toOffsetDateTime(quote.getLastTradeTime().getTime()));
				price.setPriceTimestampFromSource(true);
			} else {
				price.setPriceTimestamp(TimeUtils.toOffsetDateTime(snapshotInstant));
				price.setPriceTimestampFromSource(false);
				LOGGER.warn("PriceTimestamp was not available from source for {} {}. Used current timestamp", ticker, instrument.getExchange());
			}
			// check if the price is already in the table
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
	
	public record PurgePositionSnapshotResult(Long positionDeleteCount, Long priceDeleteCount) {/*public BeanTransformerResult (){ this(null, null); }*/};
	@Transactional
	public PurgePositionSnapshotResult purgePositionSnapshot(OffsetDateTime positionSnapshot) {
		var positionList = positionRepository.findByPositionSnapshot(positionSnapshot);
		var priceIdList = positionList.stream().map(position -> position.getPrice().getId()).collect(Collectors.toSet());
		var positionDeleteCount = positionRepository.deleteByPositionSnapshot(positionSnapshot);
		LOGGER.debug("priceIdList, size and contents: {} {}", priceIdList.size(), priceIdList);
		// we do not want to delete the Cash price records so will remove them from the list
		while (priceIdList.remove(Long.valueOf(-1)));
		while (priceIdList.remove(Long.valueOf(-2)));
		// look if among this priceIdList are used by another snapshot run
		var priceIdListUsedInAnotherSnapshot = positionRepository.selectExistingPriceIds(priceIdList);
		LOGGER.debug("existingPriceIdList, size and contents: {} {}", priceIdListUsedInAnotherSnapshot.size(), priceIdListUsedInAnotherSnapshot);
		priceIdList.removeAll(priceIdListUsedInAnotherSnapshot);
		LOGGER.debug("priceIdList, size and contents: {} {}", priceIdList.size(), priceIdList);
		
		var priceDeleteCount = priceRepository.deleteByIdIn(priceIdList);
		return new PurgePositionSnapshotResult(positionDeleteCount, priceDeleteCount);
	}
}
