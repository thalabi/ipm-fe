package com.kerneldc.ipm.rest.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.ipm.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController()
@RequestMapping("/pingController")
@RequiredArgsConstructor
@Slf4j
public class PingController {

	private final PortfolioRepository portfolioRepository;
	
    @GetMapping("/ping")
	public ResponseEntity<PingResponse> ping() {
    	LOGGER.info("Begin ...");
    	PingResponse pingResponse = new PingResponse();
    	pingResponse.setMessage("pong");
    	pingResponse.setTimestamp(LocalDateTime.now());
    	
//    	var r = portfolioRepository.findAllPortfoliosWithDependentFlags();
//    	var r0 = r.get(0);
//    	LOGGER.info("r: {}", r0.getId());
//    	LOGGER.info("r: {}", r0.getLk());
//    	LOGGER.info("r: {}", r0.getVersion());
//    	LOGGER.info("r: {}", r0.getInstitution());
//    	LOGGER.info("r: {}", r0.getAccountNumber());
//    	LOGGER.info("r: {}", r0.getName());
//    	LOGGER.info("r: {}", r0.getCurrency());
//    	LOGGER.info("r: {}", r0.getHasHoldings());
//    	LOGGER.info("r: {}", r0.getHasPositions());
    	
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(pingResponse);
    }
}

