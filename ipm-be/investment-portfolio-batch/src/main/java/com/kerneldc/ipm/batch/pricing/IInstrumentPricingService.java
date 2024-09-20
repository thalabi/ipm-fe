package com.kerneldc.ipm.batch.pricing;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;

public interface IInstrumentPricingService <D extends IInstrumentDetail> {

	Price priceInstrument(OffsetDateTime snapshotDateTime, Instrument instrument, D instrumentDetail, Map<Long, Price> priceCache) throws ApplicationException;

	Collection<InstrumentTypeEnum> canHandle();
}
