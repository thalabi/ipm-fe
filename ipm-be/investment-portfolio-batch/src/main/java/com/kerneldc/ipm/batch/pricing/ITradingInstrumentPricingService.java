package com.kerneldc.ipm.batch.pricing;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.IInstrumentDetail;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.TimeUtils;

/**
 * This interface is for pricing instruments that are tradable
 *
 */
public interface ITradingInstrumentPricingService extends IPricingService {
	
	static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public record PriceQuote(BigDecimal lastPrice, OffsetDateTime tradeTime) {}
	
	PriceQuote quote(Instrument instrument) throws ApplicationException;
	
	default Price priceInstrument(Instant snapshotInstant, Instrument instrument, IInstrumentDetail instrumentDetail, Map<Long, Price> priceCache) throws ApplicationException {
		var price = priceCache.get(instrument.getId());
		var ticker = instrument.getTicker();
		var exchange = instrument.getExchange();
		
		if (price == null) {
			
			var priceQuote = quote(instrument);

			price = new Price();
			price.setInstrument(instrument);
			if (priceQuote.tradeTime != null) {
				price.setPriceTimestamp(priceQuote.tradeTime);
				price.setPriceTimestampFromSource(true);
			} else {
				price.setPriceTimestamp(TimeUtils.toOffsetDateTime(snapshotInstant));
				price.setPriceTimestampFromSource(false);
				LOGGER.warn("PriceTimestamp was not available from source for {} {}. Used current timestamp", ticker, instrument.getExchange());
			}
			
			
			// check if the price is already in the table
			var priceList = priceRepository().findByLogicalKeyHolder(price.getLogicalKeyHolder());
			if (CollectionUtils.isNotEmpty(priceList)) {
				price = priceList.get(0);
			} else {
				price.setPrice(priceQuote.lastPrice);
			}
			LOGGER.info("Retrieved price for {} {}: {} {}", ticker, instrument.getExchange(), price.getPrice(), price.getPriceTimestamp().format(TimeUtils.DATE_TIME_FORMATTER));
			priceCache.put(instrument.getId(), price);
		} else {
			LOGGER.info("Found {} {} in price cache", ticker, exchange);
		}
		return price;
	}
	
	PriceRepository priceRepository();
}
