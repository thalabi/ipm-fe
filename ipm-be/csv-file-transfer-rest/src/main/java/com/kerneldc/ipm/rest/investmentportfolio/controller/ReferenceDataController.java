package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.TermEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/referenceDataController")
@RequiredArgsConstructor
@Slf4j
public class ReferenceDataController {

	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";

	@GetMapping("/getCurrencies")
	public ResponseEntity<List<CurrencyEnum>> getCurrencies() {
    	LOGGER.info(LOG_BEGIN);
    	var currencies = Arrays.asList(CurrencyEnum.values());
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(currencies);
    }
    @GetMapping("/getFinancialInstitutions")
	public ResponseEntity<List<FinancialInstitutionEnum>> getFinancialInstitutions() {
    	LOGGER.info(LOG_BEGIN);
    	var financialInstitutions = Arrays.asList(FinancialInstitutionEnum.values());
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(financialInstitutions);
    }
    @GetMapping("/getInstrumentTypes")
	public ResponseEntity<List<InstrumentTypeEnum>> getInstrumentTypes() {
    	LOGGER.info(LOG_BEGIN);
    	var instrumentTypes = Arrays.asList(InstrumentTypeEnum.values());
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(instrumentTypes);
    }
    @GetMapping("/getInterestBearingTypes")
	public ResponseEntity<List<InterestBearingTypeEnum>> getInterestBearingTypes() {
    	LOGGER.info(LOG_BEGIN);
    	var interestBearingTypes = Arrays.asList(InterestBearingTypeEnum.values());
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(interestBearingTypes);
    }
    @GetMapping("/getTerms")
	public ResponseEntity<List<TermEnum>> getTerms() {
    	LOGGER.info(LOG_BEGIN);
    	var terms = Arrays.asList(TermEnum.values());
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(terms);
    }
}