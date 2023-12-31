package com.kerneldc.ipm.domain.projection;

import org.springframework.data.rest.core.config.Projection;

import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMutualFund;

@Projection(name = "instrumentMutualFundInlineInstrument", types = { InstrumentMutualFund.class })
public interface IInstrumentMutualFundInlineInstrument {

	Long getId();
    String getCompany();
        
    Long getRowVersion();

	Instrument getInstrument();
}
