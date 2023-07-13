package com.kerneldc.ipm.domain.instrumentdetail;

/**
 * Interface denoting instruments that are listed on stock exchanges
 *
 */
public interface IListedInstrumentDetail extends IInstrumentDetail {

	String getExchange();
}
