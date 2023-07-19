package com.kerneldc.ipm.rest.investmentportfolio.controller;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentInterestBearingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/instrumentController")
@RequiredArgsConstructor
@Slf4j
public class InstrumentController {

	private final InstrumentRepository instrumentRepository;
	private final InstrumentInterestBearingRepository instrumentInterestBearingRepository;
	
    @PostMapping("/addInstrumentInterestBearing")
    @Transactional
    public ResponseEntity<InstrumentInterestBearingResponse> addInstrumentInterestBearing(@Valid @RequestBody InstrumentInterestBearingRequest instrumentInterestBearingRequest) {
    	LOGGER.info("Begin ...");
    	LOGGER.info("instrumentInterestBearingRequest: {}", instrumentInterestBearingRequest);
    	var i = new Instrument();
    	i.setType(instrumentInterestBearingRequest.getInstrumentType());
    	i.setTicker(instrumentInterestBearingRequest.getTicker());
    	i.setCurrency( instrumentInterestBearingRequest.getCurrency());
    	i.setName(instrumentInterestBearingRequest.getName());
    	var iib = new InstrumentInterestBearing();
    	iib.setInstrument(i);
    	iib.setType(instrumentInterestBearingRequest.getInterestBearingType());
    	iib.setFinancialInstitution(instrumentInterestBearingRequest.getFinancialInstitution());
    	iib.setPrice(instrumentInterestBearingRequest.getPrice());
    	iib.setInterestRate(instrumentInterestBearingRequest.getInterestRate());
    	iib.setTerm(instrumentInterestBearingRequest.getTerm());
    	iib.setMaturityDate(instrumentInterestBearingRequest.getMaturityDate());
    	iib.setPromotionalInterestRate(instrumentInterestBearingRequest.getPromotionalInterestRate());
    	iib.setPromotionEndDate(instrumentInterestBearingRequest.getPromotionEndDate());
    	instrumentRepository.save(i);
    	instrumentInterestBearingRepository.save(iib);
    	LOGGER.info("instrument: {}", i);
    	LOGGER.info("InstrumentInterestBearing: {}", iib);
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(new InstrumentInterestBearingResponse(StringUtils.EMPTY, i, iib));
    }
    
//    @PostMapping("/updateHolding")
//    public ResponseEntity<SaveHoldingResponse> updateHolding(@Valid @RequestBody SaveHoldingRequest saveHoldingRequest) {
//    	LOGGER.info("Begin ...");
//    	var portfolioId = saveHoldingRequest.getPortfolioId();
//    	var instrumentId = saveHoldingRequest.getInstrumentId();
//    	var holdingOptional = holdingRepository.findById(saveHoldingRequest.getId());
//    	if (holdingOptional.isEmpty()) {
//        	LOGGER.warn("Looking up holding with addHoldingRequest: {}, found holding does not exist", saveHoldingRequest);
//    		return ResponseEntity.ok(new SaveHoldingResponse("Holding does not exist", null));
//    	}
//    	
//    	var holding = holdingOptional.get();
//    	// check version
//    	if (! /* not */holding.getVersion().equals(saveHoldingRequest.getVersion())) {
//        	LOGGER.warn("Version of holding has changed since last read. Possible change by another user. saveHoldingRequest: {}", saveHoldingRequest);
//    		return ResponseEntity.ok(new SaveHoldingResponse("Version of holding has changed since last read. Possible change by another user", null));
//    	}
//    	var portfolio = portfolioRepository.findById(portfolioId).orElseThrow();
//    	holding.setPortfolio(portfolio);
//    	var instrument = instrumentRepository.findById(instrumentId).orElseThrow();
//    	holding.setInstrument(instrument);
//    	holding.setQuantity(saveHoldingRequest.getQuantity());
//    	holding.setAsOfDate(saveHoldingRequest.getAsOfDate());
//    	holding = holdingRepository.save(holding);
//    	LOGGER.info("End ...");
//    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, holding));
//    }

}
