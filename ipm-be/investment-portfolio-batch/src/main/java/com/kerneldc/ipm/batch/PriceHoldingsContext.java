package com.kerneldc.ipm.batch;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Price;

public class PriceHoldingsContext {

	private record PriceHoldingsContextRecord(Instant snapshotInstant, OffsetDateTime snapshotDateTime, Map<Long, Price> priceCache, ApplicationException applicationException) {};
	private static final ThreadLocal<PriceHoldingsContextRecord> priceHoldingsContext = new ThreadLocal<>();

	public PriceHoldingsContext() {
    	var snapshotInstant = Instant.now();
        var snapshotDateTime = OffsetDateTime.ofInstant(snapshotInstant, ZoneId.systemDefault());
        priceHoldingsContext.set(new PriceHoldingsContextRecord(snapshotInstant, snapshotDateTime, new HashMap<>(), new ApplicationException()));
	}
	public Instant getSnapshotInstant() {
		return priceHoldingsContext.get().snapshotInstant();
	}
	public OffsetDateTime getSnapshotDateTime() {
		return priceHoldingsContext.get().snapshotDateTime();
	}
	public Map<Long, Price> getPriceCache() {
		return priceHoldingsContext.get().priceCache();
	}
	public ApplicationException getApplicationException() {
		return priceHoldingsContext.get().applicationException();
	}
	
	
	
	public void removeContext() {
        priceHoldingsContext.remove();
	}
}
