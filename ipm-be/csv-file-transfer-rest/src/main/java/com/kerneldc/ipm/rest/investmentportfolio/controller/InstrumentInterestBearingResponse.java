package com.kerneldc.ipm.rest.investmentportfolio.controller;

import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstrumentInterestBearingResponse {

	private String statusMessage;
	private Instrument instrument;
	private InstrumentInterestBearing instrumentInterestBearing;
}
