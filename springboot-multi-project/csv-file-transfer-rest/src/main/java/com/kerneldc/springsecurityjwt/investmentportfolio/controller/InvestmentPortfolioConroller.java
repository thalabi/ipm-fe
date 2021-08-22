package com.kerneldc.springsecurityjwt.investmentportfolio.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.portfolio.batch.HoldingPricingService;
import com.kerneldc.portfolio.repository.HoldingDetail;
import com.kerneldc.portfolio.repository.HoldingRepository;
import com.kerneldc.springsecurityjwt.controller.PingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("investmentPortfolioConroller")
@RequiredArgsConstructor
@Slf4j
public class InvestmentPortfolioConroller {

	private final HoldingPricingService holdingPricingService;
	private final HoldingRepository holdingRepository;
	
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

    @GetMapping("/getHoldingDetails")
	public ResponseEntity<Map<String, List<HoldingDetail>>> getHoldingDetails(Long portfolioId) {
    	LOGGER.info("Begin ...");
    	var holdingDetailList = holdingRepository.findByPortfolioId(portfolioId);
    	LOGGER.info("holdingDetailList: {}", holdingDetailList);
    	holdingDetailList.stream().forEach(holdingDetail -> {
    		LOGGER.info("{} {} {} {} {} {}", holdingDetail.getInstrumentId(), holdingDetail.getTicker(), holdingDetail.getExchange(), holdingDetail.getCurrency(), holdingDetail.getName(), holdingDetail.getQuantity());
    	});
    	Map<String, List<HoldingDetail>> namedHoldingDetailList = Map.of("holdingDetails", holdingDetailList);
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(namedHoldingDetailList);
    }
}
