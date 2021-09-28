package com.kerneldc.springsecurityjwt.investmentportfolio.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.portfolio.batch.HoldingPricingService;
import com.kerneldc.portfolio.domain.Holding;
import com.kerneldc.portfolio.repository.HoldingDetail;
import com.kerneldc.portfolio.repository.HoldingRepository;
import com.kerneldc.portfolio.repository.InstrumentRepository;
import com.kerneldc.portfolio.repository.PortfolioRepository;
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
	private final PortfolioRepository portfolioRepository;
	private final InstrumentRepository instrumentRepository;
	
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
    		LOGGER.info("{} {} {} {} {} {} {} {}", holdingDetail.getId(), holdingDetail.getAsOfDate(), holdingDetail.getInstrumentId(), holdingDetail.getTicker(), holdingDetail.getExchange(), holdingDetail.getCurrency(), holdingDetail.getName(), holdingDetail.getQuantity());
    	});
    	Map<String, List<HoldingDetail>> namedHoldingDetailList = Map.of("holdingDetails", holdingDetailList);
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(namedHoldingDetailList);
    }
    
    @PostMapping("/addHolding")
    public ResponseEntity<SaveHoldingResponse> addHolding(@Valid @RequestBody SaveHoldingRequest saveHoldingRequest) {
    	LOGGER.info("Begin ...");
    	var portfolioId = saveHoldingRequest.getPortfolioId();
    	var instrumentId = saveHoldingRequest.getInstrumentId();
    	var holdingList = holdingRepository.findByPortfolioIdAndInstrumentIdAndAsOfDate(portfolioId, instrumentId, saveHoldingRequest.getAsOfDate());
    	if (CollectionUtils.isNotEmpty(holdingList)) {
        	LOGGER.warn("Looking up holding with addHoldingRequest: {}, found holding already exits", saveHoldingRequest);
    		return ResponseEntity.ok(new SaveHoldingResponse("Holding already exits", null));
    	}
    	
    	var holding = new Holding();
    	var portfolio = portfolioRepository.findById(portfolioId).orElseThrow();
    	holding.setPortfolio(portfolio);
    	var instrument = instrumentRepository.findById(instrumentId).orElseThrow();
    	holding.setInstrument(instrument);
    	holding.setQuantity(saveHoldingRequest.getQuantity());
    	holding.setAsOfDate(saveHoldingRequest.getAsOfDate());
    	holding = holdingRepository.save(holding);
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, holding));
    }
    
    @PostMapping("/updateHolding")
    public ResponseEntity<SaveHoldingResponse> updateHolding(@Valid @RequestBody SaveHoldingRequest saveHoldingRequest) {
    	LOGGER.info("Begin ...");
    	var portfolioId = saveHoldingRequest.getPortfolioId();
    	var instrumentId = saveHoldingRequest.getInstrumentId();
    	//var holdingList = holdingRepository.findByPortfolioIdAndInstrumentIdAndAsOfDate(portfolioId, instrumentId, saveHoldingRequest.getAsOfDate());
    	var holdingOptional = holdingRepository.findById(saveHoldingRequest.getId());
    	if (holdingOptional.isEmpty()) {
        	LOGGER.warn("Looking up holding with addHoldingRequest: {}, found holding does not exist", saveHoldingRequest);
    		return ResponseEntity.ok(new SaveHoldingResponse("Holding does not exist", null));
    	}
    	
    	var holding = holdingOptional.get();
    	// check version
    	if (! /* not */holding.getVersion().equals(saveHoldingRequest.getVersion())) {
        	LOGGER.warn("Version of holding has changed since last read. Possible change by another user. saveHoldingRequest: {}", saveHoldingRequest);
    		return ResponseEntity.ok(new SaveHoldingResponse("Version of holding has changed since last read. Possible change by another user", null));
    	}
    	var portfolio = portfolioRepository.findById(portfolioId).orElseThrow();
    	holding.setPortfolio(portfolio);
    	var instrument = instrumentRepository.findById(instrumentId).orElseThrow();
    	holding.setInstrument(instrument);
    	holding.setQuantity(saveHoldingRequest.getQuantity());
    	holding.setAsOfDate(saveHoldingRequest.getAsOfDate());
    	holding = holdingRepository.save(holding);
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, holding));
    }
}
