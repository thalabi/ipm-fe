package com.kerneldc.ipm.batch.pricing;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.IInstrumentDetail;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Price;

public interface IPricingService {

	Price priceInstrument(Instant snapshotInstant, Instrument instrument, IInstrumentDetail instrumentDetail, Map<Long, Price> priceCache) throws ApplicationException;

	Collection<InstrumentTypeEnum> canHandle();
}
