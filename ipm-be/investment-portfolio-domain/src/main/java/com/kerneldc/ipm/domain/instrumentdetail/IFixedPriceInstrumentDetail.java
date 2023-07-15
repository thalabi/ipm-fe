package com.kerneldc.ipm.domain.instrumentdetail;

import java.math.BigDecimal;

/**
 * Interface denoting instruments that are listed on stock exchanges
 *
 */
public interface IFixedPriceInstrumentDetail extends IInstrumentDetail {

	BigDecimal getPrice();
}
