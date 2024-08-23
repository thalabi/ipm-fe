package com.kerneldc.ipm.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.pricing.IInstrumentPricingService;
import com.kerneldc.ipm.commonservices.repository.EntityRepositoryFactory;
import com.kerneldc.ipm.commonservices.repository.EntityRepositoryFactoryHelper;
import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.HoldingPriceInterdayV;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Position;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;
import com.kerneldc.ipm.repository.HoldingPriceInterdayVRepository;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;
import com.kerneldc.ipm.util.EmailService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HoldingPricingService /*implements ApplicationRunner*/ {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private final EntityRepositoryFactory<?, ?> entityRepositoryFactory;
	private final EntityRepositoryFactoryHelper entityRepositoryFactoryHelper;
	private final ExchangeRateService exchangeRateService;
	private final EmailService emailService;
	
	private HoldingRepository holdingRepository;
	private PositionRepository positionRepository;
	private PriceRepository priceRepository;
	private HoldingPriceInterdayVRepository holdingPriceInterdayVRepository;
	
	//private Map<Long, Price> priceCache = new HashMap<>();
	
	private EnumMap<InstrumentTypeEnum, IInstrumentPricingService<IInstrumentDetail>> instrumentPricingServiceMap = new EnumMap<>(InstrumentTypeEnum.class);

	public HoldingPricingService(
			Collection<? extends IInstrumentPricingService<? extends IInstrumentDetail>> pricingServiceCollection,
			EntityRepositoryFactory<?, ?> entityRepositoryFactory,
			EntityRepositoryFactoryHelper entityRepositoryFactoryHelper, ExchangeRateService exchangeRateService,
			EmailService emailService) {

		populateInstrumentPricingServiceMap(pricingServiceCollection);
		
		LOGGER.debug("Loading instrumentPricingServiceMap with:");
		instrumentPricingServiceMap.forEach((type, pricingService) -> LOGGER.debug("[{}, {}]", type,
				pricingService.getClass().getSimpleName()));
		this.entityRepositoryFactory = entityRepositoryFactory;
		this.entityRepositoryFactoryHelper = entityRepositoryFactoryHelper;
		this.exchangeRateService = exchangeRateService;
		this.emailService = emailService;
		
		this.holdingRepository = this.entityRepositoryFactoryHelper.getHoldingRepository();
		this.positionRepository = this.entityRepositoryFactoryHelper.getPositionRepository();
		this.priceRepository = this.entityRepositoryFactoryHelper.getPriceRepository();
		this.holdingPriceInterdayVRepository = this.entityRepositoryFactoryHelper.getHoldingPriceInterdayVRepository();
	}

	@SuppressWarnings("unchecked")
	private void populateInstrumentPricingServiceMap(
			Collection<? extends IInstrumentPricingService<? extends IInstrumentDetail>> pricingServiceCollection) {
		for (var instrumentPricingService : pricingServiceCollection) {
			instrumentPricingService.canHandle();
			for (InstrumentTypeEnum type : instrumentPricingService.canHandle()) {
				instrumentPricingServiceMap.put(type, (IInstrumentPricingService<IInstrumentDetail>)instrumentPricingService);
			}
		}
	}
	
//	private record PriceHoldingsContext(Instant snapshotInstant, OffsetDateTime snapshotDateTime, ApplicationException applicationException) {};
//	private static final ThreadLocal<PriceHoldingsContext> priceHoldingsContext = new ThreadLocal<>();
	private PriceHoldingsContext priceHoldingsContext;
	public void priceHoldings(boolean sendNotifications, boolean batchProcessing) throws ApplicationException {
    	LOGGER.info("Begin ...");
//    	var snapshotInstant = Instant.now();
//        var snapshotDateTime = OffsetDateTime.ofInstant(snapshotInstant, ZoneId.systemDefault());
//        priceHoldingsContext.set(new PriceHoldingsContext(snapshotInstant, snapshotDateTime, new ApplicationException()));
    	priceHoldingsContext = new PriceHoldingsContext();
    	var priceHoldingsExceptions = new ApplicationException();

		try {
			getAndPersistExchangeRates();
		} catch (ApplicationException e) {
			var message = String.format("Unable to get and persist exchange rates: %s", e.getMessage());
			LOGGER.warn(message);
			priceHoldingsExceptions.addMessage(message);
		}

        var holdingIdList = holdingRepository.findLatestAsOfDateHoldingIds();
        var holdingList= holdingRepository.findByIdIn(holdingIdList);
        enrichHoldingList(holdingList);
        
        priceHoldingsContext.getPriceCache().clear();
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
				
				
				// get previous price and mark position as stale
				
			}
        }
        persistPrices();
        persistPositions(positionList);
        if (sendNotifications) {
        	try {
				sendPriceHoldingsNotifications(priceHoldingsExceptions);
				if (batchProcessing) {
					priceHoldingsExceptions = null; // in batch mode, clear exceptions raised so far as they are snapshotDateTime included in the email sent
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
        priceHoldingsContext.removeContext();
    	LOGGER.info("End ...");
	}

	/**
	 * Enrich the holding list with the corresponding instrument detail based on the instrument type
	 * 
	 * @param holdingList
	 */
	private void enrichHoldingList(List<Holding> holdingList) {
		// 1. build map of instrument ids (set) by type
		var instrumentIdByTypeMap = new EnumMap<InstrumentTypeEnum, Set<Long>>(InstrumentTypeEnum.class);
        for (Holding holding : holdingList) {
        	var instrumentType = holding.getInstrument().getType();
        	if (instrumentType.equals(InstrumentTypeEnum.CASH)) {
        		continue;
        	}
			instrumentIdByTypeMap.computeIfAbsent(instrumentType,
					type -> instrumentIdByTypeMap.put(type, new HashSet<>()));
    		instrumentIdByTypeMap.get(instrumentType).add(holding.getInstrument().getId());
        }
        // 2. use the map in 1) to retrieve the instrument details (eg inst_stock, inst_etc etc)) 
        //    and build map 
        var instrumentDetailByInstrumentIdMap = new HashMap<Long, IInstrumentDetail>();
        for (Map.Entry<InstrumentTypeEnum, Set<Long>> entry : instrumentIdByTypeMap.entrySet()) {
        	var inatrumentTypeEnum = entry.getKey();
        	var investmentPortfolioTableEnum = inatrumentTypeEnum.getInvestmentPortfolioTableEnum();
    		var instrumentDetailList = entityRepositoryFactory.getBaseInstrumentRepository(investmentPortfolioTableEnum).findByInstrumentIdIn(entry.getValue());
    		for (IInstrumentDetail instrumnetDetail : instrumentDetailList) {
    			instrumentDetailByInstrumentIdMap.put(instrumnetDetail.getInstrument().getId(), instrumnetDetail);
    		}
        }
        // 3. enrich holding list with the retrieved instrument details.
        for (Holding holding : holdingList) {
        	holding.setInstrumentDetail(instrumentDetailByInstrumentIdMap.get(holding.getInstrument().getId()));
        }
        
	}

	private ApplicationException checkStalePrice(Position position, ApplicationException priceHoldingsExceptions) {
		if (position.getInstrument().getType().equals(InstrumentTypeEnum.CASH)) {
			return priceHoldingsExceptions;
		}
		
		if (AppTimeUtils.compareDatePart(position.getPrice().getPriceTimestamp(), priceHoldingsContext.getSnapshotDateTime()) == -1) {
			var exceptionMessage = String.format(
					"Stale price retrieved for ticker: %s. Price date is as of %s",
					position.getInstrument().getTicker(), 
					position.getPrice().getPriceTimestamp().format(AppTimeUtils.DATE_TIME_FORMATTER));
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
		var instrumentDetail = holding.getInstrumentDetail();
		var price = getPrice(instrument, instrumentDetail);

		var position = new Position();
		position.setPositionSnapshot(priceHoldingsContext.getSnapshotDateTime());
		position.setInstrument(instrument);
		position.setPortfolio(holding.getPortfolio());
		position.setQuantity(holding.getQuantity());

		position.setPrice(price);

		return position;
	}

	private void persistPrices() throws ApplicationException {
		var priceCollection = priceHoldingsContext.getPriceCache().values();
		LOGGER.info("Price cache count: {}", priceCollection.size());
		List<Price> savedPriceList;	
		try {
			savedPriceList = priceRepository.saveAll(priceCollection);
		} catch (RuntimeException e) {
			throw new ApplicationException("Persisting prices threw an exception.", e);
		}
		LOGGER.info("Saved {} price records", savedPriceList.size());
	}

	private void getAndPersistExchangeRates() throws ApplicationException {
		
		var fromCurrency = CurrencyEnum.USD;
		var toCurrency = CurrencyEnum.CAD;
		exchangeRateService.fetchAndPersistExchangeRate(priceHoldingsContext.getSnapshotInstant(), fromCurrency, toCurrency);
	}

	private Price getPrice(Instrument instrument, IInstrumentDetail instrumentDetail) throws ApplicationException {

		var instrumentPricingService = instrumentPricingServiceMap.get(instrument.getType());
		if (instrumentPricingService == null) {
			var typeList = Stream.of(InstrumentTypeEnum.values()).map(InstrumentTypeEnum::name).toList();
			throw new IllegalArgumentException(String.format("Supported instrument types are [%s]", String.join(", ", typeList)));
		}
		
		return instrumentPricingService.priceInstrument(priceHoldingsContext.getSnapshotInstant(), instrument, instrumentDetail, priceHoldingsContext.getPriceCache());
	}
	
	
	public record PurgePositionSnapshotResult(Long positionDeleteCount, Long priceDeleteCount) {};
	/**
	 * Delete position records and associated price records for a given snapshot
	 * price records used by other snapshots are not deleted
	 * 
	 * @param positionSnapshot
	 * @return PurgePositionSnapshotResult
	 */
	@Transactional
	public PurgePositionSnapshotResult purgePositionSnapshot(OffsetDateTime positionSnapshot) {
		LOGGER.debug("trace 0");
		var positionList = positionRepository.findByPositionSnapshot(positionSnapshot);
		LOGGER.debug("trace 1");
		
		positionRepository.flush();
		positionRepository.deleteAllInBatch(positionList);
		
		LOGGER.debug("trace 2");
		// we do not want to delete the Cash price records so will remove them from the list
		var priceSet = positionList.stream().map(position -> position.getPrice()).filter(price -> price.getId().compareTo(0l) == 1).collect(Collectors.toSet());
		var priceIdList = priceSet.stream().map(price -> price.getId()).toList();
		LOGGER.debug("trace 3");
		LOGGER.debug("priceIdList, size and contents: {} {}", priceIdList.size(), priceIdList);
		// see if any of the price ids are used by another position snapshot
		var priceIdListUsedInAnotherSnapshot = positionRepository.selectExistingPriceIds(priceIdList);
		LOGGER.debug("existingPriceIdList, size and contents: {} {}", priceIdListUsedInAnotherSnapshot.size(), priceIdListUsedInAnotherSnapshot);
		priceSet.removeIf(price -> priceIdListUsedInAnotherSnapshot.contains(price.getId()));
		LOGGER.debug("priceSet, size and ids: {} {}", priceSet.size(), String.join(",", priceSet.stream().map(p -> p.getId().toString()).toList()));
		
		priceRepository.flush();
		priceRepository.deleteAllInBatch(priceSet);
		
		return new PurgePositionSnapshotResult((long)positionList.size(), (long)priceSet.size());
	}
}
