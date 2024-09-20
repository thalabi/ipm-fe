package com.kerneldc.ipm.batch.pricing;

import java.time.OffsetDateTime;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.pricing.ITradingInstrumentPricingService.PriceQuote;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.IListedInstrumentDetail;
import com.kerneldc.ipm.util.AppTimeUtils;

public interface IMarketPriceQuoteService {

	PriceQuote quote(Instrument instrument, IListedInstrumentDetail instrumentStock) throws ApplicationException;
	/**
	 * @return the priority in which the implementing class is processed
	 */
	int servicePriority();
	
	default void checkStalePrice(Instrument instrument, OffsetDateTime priceTimestamp, OffsetDateTime snapshotDateTime, ApplicationException priceHoldingsExceptions) {
//		if (position.getInstrument().getType().equals(InstrumentTypeEnum.CASH)) {
//			return;
//		}
		
		if (AppTimeUtils.differenceIsMoreThanOneDay(priceTimestamp, snapshotDateTime)) {
			
			var exceptionMessage = String.format(
					"Stale price retrieved for ticker: %s. Price date is as of %s",
					instrument.getTicker(), 
					priceTimestamp.format(AppTimeUtils.DATE_TIME_FORMATTER));
			if (! /* not */ priceHoldingsExceptions.getMessageList().contains(exceptionMessage)) {
				//LOGGER.warn(exceptionMessage);
				// TODO get logger and remove sys print
				System.out.println (exceptionMessage);
				priceHoldingsExceptions.addMessage(exceptionMessage);
			}
		}
	}

}
