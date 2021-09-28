package com.kerneldc.springsecurityjwt.investmentportfolio.controller;

import com.kerneldc.portfolio.domain.Holding;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveHoldingResponse {

	private String saveHoldingStatusMessage;
	private Holding savedHolding;
}
