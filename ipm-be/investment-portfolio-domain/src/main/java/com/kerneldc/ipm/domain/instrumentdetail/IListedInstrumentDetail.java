package com.kerneldc.ipm.domain.instrumentdetail;

import com.kerneldc.ipm.domain.ExchangeEnum;

/**
 * Interface denoting instruments that are listed on stock exchanges
 *
 */
public interface IListedInstrumentDetail extends IInstrumentDetail {

	ExchangeEnum getExchange();
}
