package com.kerneldc.ipm.domain.projection;

import org.springframework.data.rest.core.config.Projection;

import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentEtf;

@Projection(name = "instrumentEtfInlineInstrument", types = { InstrumentEtf.class })
public interface IInstrumentEtfInlineInstrument {

	Long getId();
    ExchangeEnum getExchange();
        
    Long getRowVersion();

	Instrument getInstrument();
}
