package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.repository.IPortfolioWithDependentFlags;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.repository.service.PortfolioRepositoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/portfolioController")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private final PortfolioRepository portfolioRepository;
	private final PortfolioRepositoryService portfolioRepositoryService;
	
    @GetMapping("/getPortfoliosWithDependentFlags")
	public ResponseEntity<List<IPortfolioWithDependentFlags>> getPortfoliosWithDependentFlags() {
    	LOGGER.info(LOG_BEGIN);
    	var portfolioWithDependentFlagsList = portfolioRepository.findAllPortfoliosWithDependentFlags();
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(portfolioWithDependentFlagsList);
    }
	
    @PutMapping("/savePortfolio")
	public ResponseEntity<Void> savePortfolio(
			@Valid @RequestBody Portfolio portfolio)
			throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("Portfolio: {}", portfolio);
    	portfolioRepositoryService.save(portfolio);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok().build();
    }

	@DeleteMapping("/deletePortfolio/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("id: {}", id);
    	portfolioRepositoryService.delete(id);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok().build();
    }

}
