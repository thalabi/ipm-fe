package com.kerneldc.ipm.batch.pricing;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;
import com.kerneldc.ipm.repository.PriceRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CashPriceService implements IInstrumentPricingService<IInstrumentDetail> {

	private Price CASH_CAD_PRICE;
	private Price CASH_USD_PRICE;

	public CashPriceService(PriceRepository priceRepository) {
        CASH_CAD_PRICE = priceRepository.findById(-1l).orElseThrow();
        CASH_USD_PRICE = priceRepository.findById(-2l).orElseThrow();
        LOGGER.info("CashPriceService initalized");
	}

	@Override
	public Price priceInstrument(OffsetDateTime snapshotDateTime, Instrument instrument, IInstrumentDetail instrumentDetail,
			Map<Long, Price> priceCache) throws ApplicationException {
		
		return switch (instrument.getCurrency()) {
			case CAD -> CASH_CAD_PRICE;
			case USD -> CASH_USD_PRICE;
		};
	}
	
	@Override
	public Collection<InstrumentTypeEnum> canHandle() {
		return List.of(InstrumentTypeEnum.CASH);
	}
}
