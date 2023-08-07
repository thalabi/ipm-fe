package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.InstrumentDueNotificationService;
import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.TermEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentInterestBearingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/instrumentController")
@Validated
@RequiredArgsConstructor
@Slf4j
public class InstrumentController {

	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private final InstrumentInterestBearingRepository instrumentInterestBearingRepository;
	private final InstrumentDueNotificationService instrumentDueNotificationService;
	
	@Value("${instrument.due.days.to.notify}")
	private Long daysToNotify;

    @GetMapping("/getDefaultDaysToNotify")
	public ResponseEntity<Long> getDefaultDaysToNotify() throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(daysToNotify);
    }
    @GetMapping("/triggerInstrumetDueNotification")
	public ResponseEntity<Void> triggerInstrumetDueNotification(@RequestParam @Min(1) Long daysToNotify) throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	instrumentDueNotificationService.checkDueDate(daysToNotify);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(null);
    }
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

//    @GetMapping("/getInstruments")
//	public ResponseEntity<List<InstrumentInterestBearing>> getInstruments(InstrumentTypeEnum instrumentType) throws ApplicationException {
//    	LOGGER.info(LOG_BEGIN);
//    	List<InstrumentInterestBearing> instrumentInterestBearingList;
//    	switch (instrumentType) {
//    	case INTEREST_BEARING:
//    		instrumentInterestBearingList = instrumentInterestBearingRepository.findAll();
//    		break;
//    		default:
//    			throw new ApplicationException(String.format("%s is not a valid instrument type or method not implemened", instrumentType));
//    	}
//    	LOGGER.info(LOG_END);
//		return ResponseEntity.ok(instrumentInterestBearingList);
//    }

    @PostMapping("/addInstrumentInterestBearing")
    public ResponseEntity<InstrumentInterestBearingResponse> addInstrumentInterestBearing(@Valid @RequestBody InstrumentInterestBearingRequest instrumentInterestBearingRequest) throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("instrumentInterestBearingRequest: {}", instrumentInterestBearingRequest);
    	validateInstrumentInterestBearingRequest(instrumentInterestBearingRequest);
    	var i = new Instrument();
    	i.setType(instrumentInterestBearingRequest.getInstrument().getType());
    	i.setTicker(instrumentInterestBearingRequest.getInstrument().getTicker());
    	i.setCurrency( instrumentInterestBearingRequest.getInstrument().getCurrency());
    	i.setName(instrumentInterestBearingRequest.getInstrument().getName());
    	var iib = new InstrumentInterestBearing();
    	iib.setInstrument(i);
    	iib.setType(instrumentInterestBearingRequest.getType());
    	iib.setFinancialInstitution(instrumentInterestBearingRequest.getFinancialInstitution());
    	iib.setPrice(instrumentInterestBearingRequest.getPrice());
    	iib.setInterestRate(instrumentInterestBearingRequest.getInterestRate());
    	iib.setTerm(instrumentInterestBearingRequest.getTerm());
    	iib.setMaturityDate(instrumentInterestBearingRequest.getMaturityDate());
    	iib.setPromotionalInterestRate(instrumentInterestBearingRequest.getPromotionalInterestRate());
    	iib.setPromotionEndDate(instrumentInterestBearingRequest.getPromotionEndDate());
    	iib.setEmailNotification(instrumentInterestBearingRequest.getEmailNotification());
    	instrumentInterestBearingRepository.save(iib);
    	LOGGER.info("InstrumentInterestBearing: {}", iib);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(new InstrumentInterestBearingResponse(StringUtils.EMPTY, iib));
    }

	private void validateInstrumentInterestBearingRequest(
			InstrumentInterestBearingRequest instrumentInterestBearingRequest) throws ApplicationException {
		var exception = new ApplicationException();
		switch (instrumentInterestBearingRequest.getType()) {
		case MONEY_MARKET, CHEQUING, INVESTMENT_SAVINGS:
			if (instrumentInterestBearingRequest.getTerm() != null) {
				exception.addMessage(String.format("%s interest bearing instrument can not have a term",
						instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getMaturityDate() != null) {
				exception.addMessage(String.format("%s interest bearing instrument can not have a maturity date",
						instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getPromotionalInterestRate() != null) {
				exception.addMessage(
						String.format("%s interest bearing instrument can not have a promotional interestr eate",
								instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getPromotionEndDate() != null) {
				exception.addMessage(String.format("%s interest bearing instrument can not have a prmotion end date",
						instrumentInterestBearingRequest.getType()));
			}
			if (! /* not */ exception.getMessageList().isEmpty()) {
				throw exception;
			}
			break;
		case SAVINGS:
			if (instrumentInterestBearingRequest.getTerm() != null) {
				exception.addMessage(String.format("%s interest bearing instrument can not have a term",
						instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getMaturityDate() != null) {
				exception.addMessage(String.format("%s interest bearing instrument can not have a maturity date",
						instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getPromotionalInterestRate() != null
					&& instrumentInterestBearingRequest.getPromotionEndDate() == null
					|| instrumentInterestBearingRequest.getPromotionalInterestRate() == null
							&& instrumentInterestBearingRequest.getPromotionEndDate() != null) {
				exception.addMessage(String.format(
						"For a %s interest bearing instrument, if a promotion is in effect, both the promotional interest rate and promotion end date must to be provided",
						instrumentInterestBearingRequest.getType()));
			}
			break;
		case GIC, TERM_DEPOSIT:
			if (instrumentInterestBearingRequest.getTerm() == null) {
				exception.addMessage(String.format("%s interest bearing instrument must have a term",
						instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getMaturityDate() == null) {
				exception.addMessage(String.format("%s interest bearing instrument must have a maturity date",
						instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getPromotionalInterestRate() != null) {
				exception.addMessage(
						String.format("%s interest bearing instrument can not have a promotional interestr eate",
								instrumentInterestBearingRequest.getType()));
			}
			if (instrumentInterestBearingRequest.getPromotionEndDate() != null) {
				exception.addMessage(String.format("%s interest bearing instrument can not have a prmotion end date",
						instrumentInterestBearingRequest.getType()));
			}
			break;
		}
	}
	@DeleteMapping("/deleteInstrumentInterestBearing/{id}")
    public ResponseEntity<InstrumentInterestBearingResponse> deleteInstrumentInterestBearing(@PathVariable Long id) {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("id: {}", id);
    	instrumentInterestBearingRepository.deleteById(id);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(new InstrumentInterestBearingResponse(StringUtils.EMPTY, null));
    }
    @PutMapping("/updateInstrumentInterestBearing/{id}")
    public ResponseEntity<InstrumentInterestBearingResponse> updateInstrumentInterestBearing(@PathVariable Long id, @Valid @RequestBody InstrumentInterestBearingRequest instrumentInterestBearingRequest) {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("id: {}", id);
    	var iib = instrumentInterestBearingRepository.findById(id).orElse(null);
    	if (iib == null) {
        	LOGGER.info(LOG_END);
    		return ResponseEntity.ok(new InstrumentInterestBearingResponse("Interest bearing instrument not found", null));
    	}
    	var i = iib.getInstrument();
    	i.setType(instrumentInterestBearingRequest.getInstrument().getType());
    	i.setTicker(instrumentInterestBearingRequest.getInstrument().getTicker());
    	i.setCurrency( instrumentInterestBearingRequest.getInstrument().getCurrency());
    	i.setName(instrumentInterestBearingRequest.getInstrument().getName());
    	iib.setInstrument(i);
    	iib.setType(instrumentInterestBearingRequest.getType());
    	iib.setFinancialInstitution(instrumentInterestBearingRequest.getFinancialInstitution());
    	iib.setPrice(instrumentInterestBearingRequest.getPrice());
    	iib.setInterestRate(instrumentInterestBearingRequest.getInterestRate());
    	iib.setTerm(instrumentInterestBearingRequest.getTerm());
    	iib.setMaturityDate(instrumentInterestBearingRequest.getMaturityDate());
    	iib.setPromotionalInterestRate(instrumentInterestBearingRequest.getPromotionalInterestRate());
    	iib.setPromotionEndDate(instrumentInterestBearingRequest.getPromotionEndDate());
    	iib.setEmailNotification(instrumentInterestBearingRequest.getEmailNotification());
    	instrumentInterestBearingRepository.save(iib);
    	LOGGER.info("InstrumentInterestBearing: {}", iib);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(new InstrumentInterestBearingResponse(StringUtils.EMPTY, iib));
    }
    
//    @PostMapping("/updateHolding")
//    public ResponseEntity<SaveHoldingResponse> updateHolding(@Valid @RequestBody SaveHoldingRequest saveHoldingRequest) {
//    	LOGGER.info(LOG_BEGIN);
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
//    	LOGGER.info(LOG_END);
//    	return ResponseEntity.ok(new SaveHoldingResponse(StringUtils.EMPTY, holding));
//    }

}
