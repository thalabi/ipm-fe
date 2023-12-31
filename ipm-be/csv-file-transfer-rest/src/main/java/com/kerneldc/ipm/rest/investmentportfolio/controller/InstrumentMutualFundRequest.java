package com.kerneldc.ipm.rest.investmentportfolio.controller;

import com.kerneldc.ipm.domain.Instrument;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstrumentMutualFundRequest {

	private Long id;
    @NotNull
	private Instrument instrument;
    @NotNull
    private String company;
	private Long rowVersion;
}
