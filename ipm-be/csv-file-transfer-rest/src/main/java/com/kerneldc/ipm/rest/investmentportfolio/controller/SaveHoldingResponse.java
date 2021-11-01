package com.kerneldc.ipm.rest.investmentportfolio.controller;

import com.kerneldc.ipm.domain.Holding;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveHoldingResponse {

	private String saveHoldingStatusMessage;
	private Holding savedHolding;
}
