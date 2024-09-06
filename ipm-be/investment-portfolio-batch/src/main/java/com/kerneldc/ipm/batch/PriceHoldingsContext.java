package com.kerneldc.ipm.batch;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.Price;

import lombok.Getter;
import lombok.Setter;

public class PriceHoldingsContext {

	@Getter
	private Instant snapshotInstant;
	@Getter
	private OffsetDateTime snapshotDateTime;
	@Getter
	private Map<Long, Price> priceCache;
	@Getter
	private ApplicationException priceHoldingsExceptions;
	
	@Getter @Setter
	private Holding currentHolding;
//	@Getter @Setter
//	private Instrument currentInstrument;
//	@Getter @Setter
//	private IInstrumentDetail currentInstrumentDetail;
	

	public PriceHoldingsContext() {
    	snapshotInstant = Instant.now();
        snapshotDateTime = OffsetDateTime.ofInstant(snapshotInstant, ZoneId.systemDefault());
        priceCache = new HashMap<>();
        priceHoldingsExceptions = new ApplicationException();
	}
}
