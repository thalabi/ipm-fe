package com.kerneldc.springsecurityjwt.investmentportfolio.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.portfolio.batch.HoldingPricingService;
import com.kerneldc.springsecurityjwt.controller.PingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("investmentPortfolioConroller")
@RequiredArgsConstructor
@Slf4j
public class InvestmentPortfolioConroller {

	private final HoldingPricingService holdingPricingService;
	
    @GetMapping("/priceHoldings")
	public ResponseEntity<PingResponse> priceHoldings() {
    	LOGGER.info("Begin ...");
    	var pingResponse = new PingResponse();
    	try {
			var holdingCount = holdingPricingService.priceHoldings();
	    	pingResponse.setMessage(String.format("Priced %d holdings", holdingCount));
	    	pingResponse.setTimestamp(LocalDateTime.now());
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
	    	pingResponse.setMessage(e.getMessage());
	    	pingResponse.setTimestamp(LocalDateTime.now());
		}
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(pingResponse);
    }

}
