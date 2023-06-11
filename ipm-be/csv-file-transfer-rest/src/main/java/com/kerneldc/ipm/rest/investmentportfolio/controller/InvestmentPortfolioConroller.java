package com.kerneldc.ipm.rest.investmentportfolio.controller;

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

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.HoldingPricingService;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.repository.HoldingDetail;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PositionSnapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/investmentPortfolioConroller")
@RequiredArgsConstructor
@Slf4j
public class InvestmentPortfolioConroller {

	private final HoldingPricingService holdingPricingService;
	private final HoldingRepository holdingRepository;
	private final PortfolioRepository portfolioRepository;
	private final InstrumentRepository instrumentRepository;
	private final PositionRepository positionRepository;
	
    @GetMapping("/priceHoldings")
	public ResponseEntity<PriceHoldingResponse> priceHoldings(Boolean sendEmail) {
    	LOGGER.info("Begin ...");
    	var priceHoldingResponse = new PriceHoldingResponse();
    	try {
			holdingPricingService.priceHoldings(sendEmail, false);
	    	priceHoldingResponse.setMessage(StringUtils.EMPTY);
	    	priceHoldingResponse.setTimestamp(LocalDateTime.now());
		} catch (ApplicationException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Exception pricing holdings:\n", e);
	    	priceHoldingResponse.setMessage(e.getMessage());
	    	priceHoldingResponse.setTimestamp(LocalDateTime.now());
		}
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(priceHoldingResponse);
    }

    @GetMapping("/getHoldingDetails")
	public ResponseEntity<Map<String, List<HoldingDetail>>> getHoldingDetails(Long portfolioId) {
    	LOGGER.info("Begin ...");
    	var holdingDetailList = holdingRepository.findByPortfolioId(portfolioId);
    	LOGGER.info("holdingDetailList: {}", holdingDetailList);
    	holdingDetailList.stream().forEach(holdingDetail ->
    		LOGGER.info("{} {} {} {} {} {} {} {}", holdingDetail.getId(), holdingDetail.getAsOfDate(), holdingDetail.getInstrumentId(), holdingDetail.getTicker(), holdingDetail.getExchange(), holdingDetail.getCurrency(), holdingDetail.getName(), holdingDetail.getQuantity())
    	);
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

    @GetMapping("/getDistinctPositionSnapshots")
	public ResponseEntity<List<PositionSnapshot>> getDistinctPositionSnapshots() {
    	LOGGER.info("Begin ...");
    	
    	var positionSnapshotList = positionRepository.selectAllPositionSnapshots();
       	LOGGER.info("End ...");
    	return ResponseEntity.ok(positionSnapshotList);
    }
    
    @PostMapping("/purgePositionSnapshot")
    public ResponseEntity<String> purgePositionSnapshot(@Valid @RequestBody PurgePositionSnapshotRequest purgePositionSnapshotRequest) {
    	LOGGER.info("Begin ...");
    	
    	LOGGER.info("purgePositionSnapshotRequest.getPositionSnapshot(): {}", purgePositionSnapshotRequest.getPositionSnapshot());
		var purgePositionSnapshotResult = holdingPricingService.purgePositionSnapshot(purgePositionSnapshotRequest.getPositionSnapshot());
		LOGGER.info("Purged {} position and {} price records.", purgePositionSnapshotResult.positionDeleteCount(),
				purgePositionSnapshotResult.priceDeleteCount());
    	
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }

}
