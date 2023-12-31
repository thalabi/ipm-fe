package com.kerneldc.ipm.rest.investmentportfolio.controller;

import com.kerneldc.ipm.domain.ExchangeEnum;
import com.kerneldc.ipm.domain.Instrument;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstrumentStockRequest {

	private Long id;
    @NotNull
	private Instrument instrument;
    @NotNull
    private ExchangeEnum exchange;
	private Long rowVersion;
}
