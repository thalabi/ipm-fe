package com.kerneldc.ipm.domain.projection;

import org.springframework.data.rest.core.config.Projection;

import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;

@Projection(name = "instrumentStockInlineInstrument", types = { InstrumentStock.class })
public interface IInstrumentStockInlineInstrument {

	Long getId();
    ExchangeEnum getExchange();
        
    Long getRowVersion();

	Instrument getInstrument();
}
