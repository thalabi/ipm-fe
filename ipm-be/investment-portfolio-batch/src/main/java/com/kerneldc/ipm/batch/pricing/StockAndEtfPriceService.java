package com.kerneldc.ipm.batch.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockAndEtfPriceService implements ITradingInstrumentPricingService<IListedInstrumentDetail> {

	private List<IMarketPriceQuoteService> marketPriceQuoteServices;
	private final PriceRepository priceRepository;
	
	protected Table<String, ExchangeEnum, Float> fallbackPriceLookupTable = HashBasedTable.create();
	private Map<Long, Price> latestPriceByInstrumentId;
	public StockAndEtfPriceService(Collection<IMarketPriceQuoteService> marketPriceQuoteServices, PriceRepository priceRepository) {
		
		// sort price quote services by priority. 1 being top priority
		this.marketPriceQuoteServices = marketPriceQuoteServices.stream().sorted(Comparator.comparingInt(IMarketPriceQuoteService::servicePriority)).toList();
		this.marketPriceQuoteServices.forEach((mpqs) -> LOGGER.info("Loaded impl of IMarketPriceQuoteService [{}]", mpqs.getClass().getSimpleName()));
		this.priceRepository = priceRepository;
		
		fallbackPriceLookupTable.put("SENS", ExchangeEnum.CNSX, 0.01f);
		fallbackPriceLookupTable.put("BHCC", ExchangeEnum.CNSX, 0.025f);
		
		latestPriceByInstrumentId = priceRepository.findLatestPriceList().stream()
				.collect(Collectors.toMap(p -> p.getInstrument().getId(), Function.identity()));
	}
	
	@Override
	public PriceQuote quote(OffsetDateTime snapshotDateTime, Instrument instrument, IListedInstrumentDetail instrumentStock) {
		for (IMarketPriceQuoteService marketPriceQuoteService: marketPriceQuoteServices) {
			try {
				var priceQuote = marketPriceQuoteService.quote(instrument, instrumentStock);
				checkStalePrice(instrument, priceQuote.tradeTime(), snapshotDateTime);
				return priceQuote;
			} catch (ApplicationException e) {
				LOGGER.warn("Impl of IMarketPriceQuoteService [{}] failed with [{}]", marketPriceQuoteService.getClass().getSimpleName(), e.getMessage());
			}
		}
		return fallBackToLatestAvailablePrice(instrument);
	}

	private void checkStalePrice(Instrument instrument, OffsetDateTime priceTimestamp, OffsetDateTime snapshotDateTime) throws ApplicationException {
		if (AppTimeUtils.differenceIsMoreThanOneDay(priceTimestamp, snapshotDateTime)) {
			
			var exceptionMessage = String.format(
					"Stale price retrieved for ticker: %s. Price date is as of %s",
					instrument.getTicker(), 
					priceTimestamp.format(AppTimeUtils.DATE_TIME_FORMATTER));
			LOGGER.warn(exceptionMessage);
			throw new ApplicationException(exceptionMessage);
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
	
	@Override
	public Collection<InstrumentTypeEnum> canHandle() {
		return List.of(InstrumentTypeEnum.STOCK, InstrumentTypeEnum.ETF);
	}

	@Override
	public PriceRepository priceRepository() {
		return priceRepository;
	}
}
