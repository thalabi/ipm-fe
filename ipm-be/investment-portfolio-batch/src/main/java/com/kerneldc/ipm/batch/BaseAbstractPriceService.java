package com.kerneldc.ipm.batch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.TimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseAbstractPriceService {

	private final PriceRepository priceRepository;

	protected record PriceQuote(BigDecimal lastPrice, OffsetDateTime tradeTime) {}

	public Price getSecurityPrice(Instant snapshotInstant, Instrument instrument, Map<Long, Price> priceCache) throws ApplicationException {
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
			var priceList = priceRepository.findByLogicalKeyHolder(price.getLogicalKeyHolder());
			if (CollectionUtils.isNotEmpty(priceList)) {
				price = priceList.get(0);
			} else {
				price.setPrice(priceQuote.lastPrice);
			}
			LOGGER.info("Retrieved price for {} {}: {} {}", ticker, instrument.getExchange(), price.getPrice(), price.getPriceTimestamp());
			priceCache.put(instrument.getId(), price);
		} else {
			LOGGER.info("Found {} {} in price cache", ticker, exchange);
		}
		return price;
	}

	public abstract PriceQuote quote(Instrument instrument) throws ApplicationException ;
}
