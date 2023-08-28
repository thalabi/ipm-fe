package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.HoldingPricingService;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.IHoldingDetail;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PositionSnapshot;
import com.kerneldc.ipm.repository.service.HoldingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/investmentPortfolioController")
@RequiredArgsConstructor
@Slf4j
public class InvestmentPortfolioController {

	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";

	private final HoldingPricingService holdingPricingService;
	private final HoldingService holdingService;
	private final HoldingRepository holdingRepository;
	private final PortfolioRepository portfolioRepository;
	private final InstrumentRepository instrumentRepository;
	private final PositionRepository positionRepository;
	
    @GetMapping("/priceHoldings")
	public ResponseEntity<BatchJobResponse> priceHoldings(Boolean sendEmail) {
    	LOGGER.info(LOG_BEGIN);
    	var priceHoldingResponse = new BatchJobResponse();
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
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(priceHoldingResponse);
    }

    @GetMapping("/getHoldingDetails")
	public ResponseEntity<Map<String, List<IHoldingDetail>>> getHoldingDetails(Long portfolioId) {
    	LOGGER.info(LOG_BEGIN);
    	var holdingDetailList = holdingRepository.findByPortfolioId(portfolioId);
    	LOGGER.info("holdingDetailList: {}", holdingDetailList);
    	holdingDetailList.stream().forEach(holdingDetail ->
    		LOGGER.info("{} {} {} {} {} {} {} {}", holdingDetail.getId(), holdingDetail.getAsOfDate(), holdingDetail.getInstrumentId(), holdingDetail.getTicker(), /*holdingDetail.getExchange(),*/ holdingDetail.getCurrency(), holdingDetail.getName(), holdingDetail.getQuantity())
    	);
    	Map<String, List<IHoldingDetail>> namedHoldingDetailList = Map.of("holdingDetails", holdingDetailList);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(namedHoldingDetailList);
    }
    
    @PostMapping("/addHolding")
    public ResponseEntity<SaveHoldingResponse> addHolding(@Valid @RequestBody SaveHoldingRequest saveHoldingRequest) {
    	LOGGER.info(LOG_BEGIN);
    	var holding = copyToHolding(saveHoldingRequest);
    	holding = holdingService.save(holding);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, holding));
    }
    
    private Holding copyToHolding(@Valid SaveHoldingRequest saveHoldingRequest) {
    	var holding = new Holding();
    	holding.setId(saveHoldingRequest.getId());
    	
    	var instrument = new Instrument();
    	instrument.setId(saveHoldingRequest.getInstrumentId());
    	instrument.setVersion(0l);

    	holding.setInstrument(instrument);

    	var portfolio = new Portfolio();
    	portfolio.setId(saveHoldingRequest.getPortfolioId());
    	portfolio.setVersion(0l);
    	
    	holding.setPortfolio(portfolio);
    	
    	holding.setQuantity(saveHoldingRequest.getQuantity());
    	holding.setAsOfDate(saveHoldingRequest.getAsOfDate());
    	
    	holding.setVersion(saveHoldingRequest.getVersion());
		return holding;
	}

	@PostMapping("/updateHolding")
    public ResponseEntity<SaveHoldingResponse> updateHolding(@Valid @RequestBody SaveHoldingRequest saveHoldingRequest) {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("saveHoldingRequest: {}", saveHoldingRequest);
    	var holding = copyToHolding(saveHoldingRequest);
    	holding = holdingService.save(holding);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, holding));
    }

	@DeleteMapping("/deleteHolding/{id}")
    public ResponseEntity<SaveHoldingResponse> deleteHolding(@PathVariable Long id) {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("id: {}", id);
    	holdingService.delete(id);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, null));
    }

    @GetMapping("/getDistinctPositionSnapshots")
	public ResponseEntity<List<PositionSnapshot>> getDistinctPositionSnapshots() {
    	LOGGER.info(LOG_BEGIN);
    	
    	var positionSnapshotList = positionRepository.selectAllPositionSnapshots();
       	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(positionSnapshotList);
    }
    
    @PostMapping("/purgePositionSnapshot")
    public ResponseEntity<String> purgePositionSnapshot(@Valid @RequestBody PurgePositionSnapshotRequest purgePositionSnapshotRequest) {
    	LOGGER.info(LOG_BEGIN);
    	
    	LOGGER.info("purgePositionSnapshotRequest.getPositionSnapshot(): {}", purgePositionSnapshotRequest.getPositionSnapshot());
		var purgePositionSnapshotResult = holdingPricingService.purgePositionSnapshot(purgePositionSnapshotRequest.getPositionSnapshot());
		LOGGER.info("Purged {} position and {} price records.", purgePositionSnapshotResult.positionDeleteCount(),
				purgePositionSnapshotResult.priceDeleteCount());
    	
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(StringUtils.EMPTY);
    }

}
