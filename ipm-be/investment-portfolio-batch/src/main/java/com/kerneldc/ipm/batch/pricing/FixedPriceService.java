package com.kerneldc.ipm.batch.pricing;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IFixedPriceInstrumentDetail;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixedPriceService implements IInstrumentPricingService<IFixedPriceInstrumentDetail> {

	private final PriceRepository priceRepository;

	@Override
	public Price priceInstrument(Instant snapshotInstant, Instrument instrument, IFixedPriceInstrumentDetail instrumentDetail,
			Map<Long, Price> priceCache) throws ApplicationException {
		var price = priceCache.get(instrument.getId());
		var ticker = instrument.getTicker();

		if (price == null) {
			price = new Price();
			price.setInstrument(instrument);
			price.setPriceTimestamp(AppTimeUtils.toOffsetDateTime(snapshotInstant));
			price.setPriceTimestampFromSource(true);

			// check if the price is already in the table
			var priceList = priceRepository.findByLogicalKeyHolder(price.getLogicalKeyHolder());
			if (CollectionUtils.isNotEmpty(priceList)) {
				price = priceList.get(0);
			} else {
				price.setPrice(instrumentDetail.getPrice());
			}
			LOGGER.info("Retrieved price for {}: {} {}", ticker, price.getPrice(),
					price.getPriceTimestamp().format(AppTimeUtils.DATE_TIME_FORMATTER));
			priceCache.put(instrument.getId(), price);
		
		} else {
			LOGGER.info("Found {} in price cache", ticker);
		}
		return price;
	}
	
	@Override
	public Collection<InstrumentTypeEnum> canHandle() {
		return List.of(InstrumentTypeEnum.INTEREST_BEARING, InstrumentTypeEnum.BOND);
	}
}
