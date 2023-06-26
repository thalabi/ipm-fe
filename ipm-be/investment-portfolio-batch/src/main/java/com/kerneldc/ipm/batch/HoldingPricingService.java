package com.kerneldc.ipm.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.CurrencyEnum;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingPricingService /*implements ApplicationRunner*/ {
	private static final String CASH_TICKER = "CASH";

	private static final boolean OFFLINE_MODE = false;
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private final HoldingRepository holdingRepository;
	private final PositionRepository positionRepository;
	private final PriceRepository priceRepository;
	private final HoldingPriceInterdayVRepository holdingPriceInterdayVRepository;
	private final ExchangeRateService exchangeRateService;
	private final MutualFundPriceService mutualFundPriceService;
	private final StockPriceService stockPriceService;
	private final EmailService emailService;
	
	private Instant snapshotInstant;
	private OffsetDateTime now;
	
	private Price CASH_CAD_PRICE;
	private Price CASH_USD_PRICE;
	
	private Map<Long, Price> priceCache = new HashMap<>();

	public void priceHoldings(boolean sendNotifications, boolean batchProcessing) throws ApplicationException {
        snapshotInstant = Instant.now();
        now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        var priceHoldingsExceptions = new ApplicationException();
        
        CASH_CAD_PRICE = priceRepository.findById(-1l).orElseThrow();
        CASH_USD_PRICE = priceRepository.findById(-2l).orElseThrow();


		if (! /* not */ OFFLINE_MODE) {
			try {
				getAndPersistExchangeRates();
			} catch (ApplicationException e) {
				var message = String.format("Unable to get and persist exchange rates: %s", e.getMessage());
				LOGGER.warn(message);
				priceHoldingsExceptions.addMessage(message);
			}
		}

        var holdingList = holdingRepository.findLatestAsOfDateHoldings();
        priceCache.clear();
        var positionList = new ArrayList<Position>();
        for (Holding holding : holdingList) {
        	Position position = null;
        	try {
        		position = getHoldingPrice(holding);
				positionList.add(position);

				// Check that the price is not stale ie is as of today.
				priceHoldingsExceptions = checkStalePrice(position, priceHoldingsExceptions);
				
			} catch (ApplicationException e) {
				LOGGER.warn(e.getMessage());
				priceHoldingsExceptions.addMessage(e.getMessage());
			}
        }
        persistPrices();
        persistPositions(positionList);
        if (sendNotifications) {
        	try {
				sendPriceHoldingsNotifications(priceHoldingsExceptions);
				if (batchProcessing) {
					priceHoldingsExceptions = null; // in batch mode, clear exceptions raised so far as they are now included in the email sent
				}
			} catch (ApplicationException e) {
				var message = String.format("Unable to send price holdings notification: %s", e.getMessage());
				LOGGER.warn(message);
				priceHoldingsExceptions.addMessage(message);
			}
        }
        
        if (priceHoldingsExceptions!= null && CollectionUtils.isNotEmpty(priceHoldingsExceptions.getMessageList())) {
        	throw priceHoldingsExceptions;
        }
	}

	private ApplicationException checkStalePrice(Position position, ApplicationException priceHoldingsExceptions) {
		if (StringUtils.equals(position.getInstrument().getTicker(), CASH_TICKER)) {
			return priceHoldingsExceptions;
		}
		if (TimeUtils.compareDatePart(position.getPrice().getPriceTimestamp(), now) == -1) {
			var exceptionMessage = String.format("Stale price retrieved for ticker: %s and exchange: %s. Price date is as of %s", position.getInstrument().getTicker(), position.getInstrument().getExchange(), position.getPrice().getPriceTimestamp().format(TimeUtils.DATE_TIME_FORMATTER));
			if (! /* not */ priceHoldingsExceptions.getMessageList().contains(exceptionMessage)) {
				LOGGER.warn(exceptionMessage);
				priceHoldingsExceptions.addMessage(exceptionMessage);
			}
		}
		return priceHoldingsExceptions;
	}
	
	private void sendPriceHoldingsNotifications(ApplicationException priceHoldingsExceptions) throws ApplicationException {
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
		
		Float percentChange; 
		if (CollectionUtils.isEmpty(nMarketValues)) {
			percentChange = null;
		} else {
			var yesterdaysMarketValue = nMarketValues.get(nMarketValues.size()-1).getMarketValue();
			percentChange = todaysMarketValue.subtract(yesterdaysMarketValue).divide(yesterdaysMarketValue, RoundingMode.HALF_UP).multiply(ONE_HUNDRED).floatValue();
		}
		emailService.sendDailyMarketValueNotification(todaysSnapshot, todaysMarketValue, percentChange, nMarketValues, priceHoldingsExceptions);
	}

	private void persistPositions(ArrayList<Position> positionList) {
		var savedPositionList = positionRepository.saveAll(positionList);
		LOGGER.info("Saved {} position records", savedPositionList.size());
	}

	private Position getHoldingPrice(Holding holding) throws ApplicationException {
		var instrument = holding.getInstrument();
		var price = getPrice(instrument);

		var position = new Position();
		position.setPositionSnapshot(OffsetDateTime.ofInstant(snapshotInstant, ZoneId.systemDefault()));
		position.setInstrument(instrument);
		position.setPortfolio(holding.getPortfolio());
		position.setQuantity(holding.getQuantity());

		position.setPrice(price);

		return position;
	}

	private void persistPrices() {
		var priceCollection = priceCache.values();
		LOGGER.info("Price cache count: {}", priceCollection.size());
			
		var savedPriceList = priceRepository.saveAll(priceCollection);
		LOGGER.info("Saved {} price records", savedPriceList.size());
	}

	private void getAndPersistExchangeRates() throws ApplicationException {
		
		var fromCurrency = CurrencyEnum.USD;
		var toCurrency = CurrencyEnum.CAD;
		exchangeRateService.retrieveAndPersistExchangeRate(snapshotInstant, fromCurrency, toCurrency);
	}

	private Price getPrice(Instrument instrument) throws ApplicationException {
		
		if (OFFLINE_MODE) {
			return CASH_CAD_PRICE;
		}

		switch (instrument.getType()) {
		case CASH:
			switch (instrument.getCurrency()) {
			case CAD:
				return CASH_CAD_PRICE;
			case USD:
				return CASH_USD_PRICE;
			default:
				throw new IllegalArgumentException("Currency should be either CAD or USD");
			}
		case STOCK, ETF:
			return stockPriceService.getSecurityPrice(snapshotInstant, instrument, priceCache);
		case MUTUAL_FUND:
			return mutualFundPriceService.getSecurityPrice(snapshotInstant, instrument, priceCache);
		default:
			throw new IllegalArgumentException("Supported instruments types are CASH, STOCK & MUTUAL_FUND");
		}
//		if (StringUtils.equals(instrument.getTicker(), CASH_TICKER)) {
//			switch (instrument.getCurrency()) {
//				case CAD:
//					return CASH_CAD_PRICE;
//				case USD:
//					return CASH_USD_PRICE;
//				default:
//					throw new IllegalArgumentException("Currency should be either CAD or USD");
//			}
//		} else {
//			if (instrument.getExchange().equals("CF")) { // CF -> Canadian Fund
//				return mutualFundPriceService.getSecurityPrice(snapshotInstant, instrument, priceCache);
//			} else {
//				return stockPriceService.getSecurityPrice(snapshotInstant, instrument, priceCache);
//			}
//		}
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
