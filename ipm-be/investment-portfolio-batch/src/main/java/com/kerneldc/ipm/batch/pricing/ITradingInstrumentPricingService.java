package com.kerneldc.ipm.batch.pricing;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

/**
 * This interface is for pricing instruments that are tradable
 *
 */
public interface ITradingInstrumentPricingService<D extends IInstrumentDetail> extends IInstrumentPricingService<D> {
	
	static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public record PriceQuote(BigDecimal lastPrice, OffsetDateTime tradeTime) {}
	
	PriceQuote quote(OffsetDateTime snapshotDateTime, Instrument instrument, D instrumentDetail) throws ApplicationException;
	
	@Override
	default Price priceInstrument(OffsetDateTime snapshotDateTime, Instrument instrument, D instrumentDetail, Map<Long, Price> priceCache) throws ApplicationException {
		var ticker = instrument.getTicker();
		var tradedInstrumentDetail = (IListedInstrumentDetail)instrumentDetail;
		var exchange = tradedInstrumentDetail.getExchange();
		
		var price = priceCache.get(instrument.getId());
		if (price == null) {
			
			var priceQuote = quote(snapshotDateTime, instrument, instrumentDetail);

			price = new Price();
			price.setInstrument(instrument);
			if (priceQuote.tradeTime != null) {
				price.setPriceTimestamp(priceQuote.tradeTime);
				price.setPriceTimestampFromSource(true);
			} else {
				price.setPriceTimestamp(snapshotDateTime);
				price.setPriceTimestampFromSource(false);
				LOGGER.warn("PriceTimestamp was not available from source for {} {}. Used current timestamp", ticker,
						exchange);
			}
			
			
			// check if the price is already in the table
			var priceList = priceRepository().findByLogicalKeyHolder(price.getLogicalKeyHolder());
			if (CollectionUtils.isNotEmpty(priceList)) {
				price = priceList.get(0);
			} else {
				price.setPrice(priceQuote.lastPrice);
			}
			
			LOGGER.info("Retrieved price for {} {}: {} {}", ticker, exchange, price.getPrice(),
					LOGGER.isInfoEnabled() ? price.getPriceTimestamp().format(AppTimeUtils.DATE_TIME_FORMATTER) : null);
			priceCache.put(instrument.getId(), price);
		} else {
			LOGGER.info("Found {} {} in price cache", ticker, exchange);
		}
		return price;
	}
	
	PriceRepository priceRepository();
}
