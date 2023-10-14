package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.server.ResponseStatusException;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.FixedIncomeInstrumentReportService;
import com.kerneldc.ipm.batch.InstrumentDueNotificationService;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.repository.service.InstrumentInterestBearingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
	private final InstrumentInterestBearingService instrumentInterestBearingService;
	private final InstrumentDueNotificationService instrumentDueNotificationService;
	private final FixedIncomeInstrumentReportService fixedIncomeInstrumentReportService;

	@Value("${instrument.due.days.to.notify}")
	private Long daysToNotify;

    @GetMapping("/getDefaultDaysToNotify")
	public ResponseEntity<Long> getDefaultDaysToNotify() throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(daysToNotify);
    }
    
    @GetMapping("/triggerInstrumetDueNotification")
	public ResponseEntity<BatchJobResponse> triggerInstrumetDueNotification(@RequestParam @Min(1) Long daysToNotify) throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	var checkDueDateResponse = new BatchJobResponse();
    	try {
    		instrumentDueNotificationService.checkDueDate(daysToNotify);
	    	checkDueDateResponse.setMessage(StringUtils.EMPTY);
	    	checkDueDateResponse.setTimestamp(LocalDateTime.now());
		} catch (ApplicationException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Exception checking instrument due dates:\n", e);
	    	checkDueDateResponse.setMessage(e.getMessage());
	    	checkDueDateResponse.setTimestamp(LocalDateTime.now());
		}
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(checkDueDateResponse);	
    }

    @PutMapping("/saveInstrumentInterestBearing")
	public ResponseEntity<Void> saveInstrumentInterestBearing(
			@Valid @RequestBody InstrumentInterestBearingRequest instrumentInterestBearingRequest)
			throws ApplicationException {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("instrumentInterestBearingRequest: {}", instrumentInterestBearingRequest);
    	validateInstrumentInterestBearingRequest(instrumentInterestBearingRequest);
    	var iib = copyToInstrumentInterestBearing(instrumentInterestBearingRequest);
    	instrumentInterestBearingService.save(iib);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok().body(null);
    }
    
	@DeleteMapping("/deleteInstrumentInterestBearing/{id}")
    public ResponseEntity<Void> deleteInstrumentInterestBearing(@PathVariable Long id) {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("id: {}", id);
    	instrumentInterestBearingService.delete(id);
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok().body(null);
    }
	
    @PostMapping("/generateFixedIncomeInstrumentReport")
	public ResponseEntity<ReportJobResponse> generateFixedIncomeInstrumentReport(@RequestParam @NotNull @Pattern(regexp = "Download|Email", flags = Pattern.Flag.CASE_INSENSITIVE) String reportDisposition) {
    	LOGGER.info(LOG_BEGIN);
    	LOGGER.info("reportDisposition: {}", reportDisposition);
    	var reportJobResponse = new ReportJobResponse();
    	try {
			var report = reportDisposition.equalsIgnoreCase("Download") ? fixedIncomeInstrumentReportService.generate()
					: fixedIncomeInstrumentReportService.generateAndEmail();
			reportJobResponse.setFilename(report.getName());
			reportJobResponse.setTimestamp(LocalDateTime.now());
		} catch (ApplicationException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Exception generating fixed income instrument report:\n", e);
			reportJobResponse.setFilename(e.getMessage());
			reportJobResponse.setTimestamp(LocalDateTime.now());
		}
    	LOGGER.info(LOG_END);
    	return ResponseEntity.ok(reportJobResponse);
    }
    
	@GetMapping("/downloadFixedIncomeInstrumentReport")
	public ResponseEntity<byte[]> downloadFixedIncomeInstrumentReport(@RequestParam @NotNull String filename) {
    	LOGGER.info(LOG_BEGIN);
		LOGGER.info("filename: {}", filename);
    	var path = Paths.get(FileUtils.getTempDirectory().getPath(), filename);
    	byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(path);
		} catch (NoSuchFileException e) {
			LOGGER.error("Exception downloading fixed income instrument report:\n", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "NoSuchFileException: "+e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error("Exception downloading fixed income instrument report:\n", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
		LOGGER.info(LOG_END);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(
						MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytes);
    }

    private InstrumentInterestBearing copyToInstrumentInterestBearing(
    		@Valid InstrumentInterestBearingRequest instrumentInterestBearingRequest) {
    	var iib = new InstrumentInterestBearing();
    	var i = new Instrument();
    	i.setId(instrumentInterestBearingRequest.getInstrument().getId());
    	i.setType(instrumentInterestBearingRequest.getInstrument().getType());
    	i.setTicker(instrumentInterestBearingRequest.getInstrument().getTicker());
    	i.setCurrency( instrumentInterestBearingRequest.getInstrument().getCurrency());
    	i.setName(instrumentInterestBearingRequest.getInstrument().getName());
    	i.setNotes(instrumentInterestBearingRequest.getInstrument().getNotes());
    	i.setVersion(instrumentInterestBearingRequest.getInstrument().getRowVersion());
    	iib.setId(instrumentInterestBearingRequest.getId());
    	iib.setInstrument(i);
    	iib.setType(instrumentInterestBearingRequest.getType());
    	iib.setFinancialInstitution(instrumentInterestBearingRequest.getFinancialInstitution());
    	iib.setPrice(instrumentInterestBearingRequest.getPrice());
    	iib.setInterestRate(instrumentInterestBearingRequest.getInterestRate());
    	iib.setTerm(instrumentInterestBearingRequest.getTerm());
    	iib.setMaturityDate(instrumentInterestBearingRequest.getMaturityDate());
    	iib.setNextPaymentDate(instrumentInterestBearingRequest.getNextPaymentDate());
    	iib.setPromotionalInterestRate(instrumentInterestBearingRequest.getPromotionalInterestRate());
    	iib.setPromotionEndDate(instrumentInterestBearingRequest.getPromotionEndDate());
    	iib.setEmailNotification(instrumentInterestBearingRequest.getEmailNotification());
    	iib.setAccountNumber(instrumentInterestBearingRequest.getAccountNumber());
    	iib.setHolder(instrumentInterestBearingRequest.getHolder());
    	iib.setRegisteredAccount(instrumentInterestBearingRequest.getRegisteredAccount());
    	iib.setVersion(instrumentInterestBearingRequest.getRowVersion());
    	return iib;
    }
	private void validateInstrumentInterestBearingRequest(
			InstrumentInterestBearingRequest instrumentInterestBearingRequest) throws ApplicationException {
		var exception = new ApplicationException();
		
		// TODO refactor to validate each individual field
		
		switch (instrumentInterestBearingRequest.getType()) {
			case MONEY_MARKET, INVESTMENT_SAVINGS -> {
				if (instrumentInterestBearingRequest.getHolder() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a holder",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getAccountNumber() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have an account number",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getRegisteredAccount() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have an registered account field",
							instrumentInterestBearingRequest.getType()));
				}
			}
			case CHEQUING, SAVINGS, GIC, TERM_DEPOSIT -> {
				if (instrumentInterestBearingRequest.getHolder() == null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a holder",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getAccountNumber() == null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have an account number",
							instrumentInterestBearingRequest.getType()));
				}
			}
		}
		
		switch (instrumentInterestBearingRequest.getType()) {
			case MONEY_MARKET, CHEQUING, INVESTMENT_SAVINGS -> {
				if (instrumentInterestBearingRequest.getTerm() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a term",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getMaturityDate() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a maturity date",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getNextPaymentDate() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a next payment date",
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
			}
			case SAVINGS -> {
				if (instrumentInterestBearingRequest.getTerm() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a term",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getMaturityDate() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a maturity date",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getNextPaymentDate() != null) {
					exception.addMessage(String.format("%s interest bearing instrument can not have a next payment date",
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
			}
			case GIC, TERM_DEPOSIT -> {
				if (instrumentInterestBearingRequest.getTerm() == null) {
					exception.addMessage(String.format("%s interest bearing instrument must have a term",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getMaturityDate() == null) {
					exception.addMessage(String.format("%s interest bearing instrument must have a maturity date",
							instrumentInterestBearingRequest.getType()));
				}
				if (instrumentInterestBearingRequest.getNextPaymentDate() == null) {
					exception.addMessage(String.format("%s interest bearing instrument must have a next payment date",
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
			}
		}
	}
	
}
