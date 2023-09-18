package com.kerneldc.ipm.rest;

import java.sql.SQLException;

import javax.validation.constraints.NotNull;

import org.hibernate.JDBCException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kerneldc.common.exception.RecordIntegrityViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/testController")
@Validated
@RequiredArgsConstructor
@Slf4j
public class TestController {
	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";

	@GetMapping("/testEndpoint")
	public ResponseEntity<Void> testEndpoint(@RequestParam @NotNull Integer testCaseNumber) {
    	LOGGER.info(LOG_BEGIN);
		LOGGER.info("testCase: {}", testCaseNumber);
		switch (testCaseNumber) {
		case 1:
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Custom exception message: ");
		case 2:
			// to be handled by RestControllerExceptionHandler
			throw new EmptyResultDataAccessException(1);
		case 3:
			var sqlException = new SQLException("sqlException message");
			var jdbcException = new JDBCException("jdbcException message", sqlException);
			var dataIntegrityViolationException = new DataIntegrityViolationException("dataIntegrityViolationException message", jdbcException);
			throw new RecordIntegrityViolationException(dataIntegrityViolationException);
		case 4:
			break;
		default:
			throw new IllegalArgumentException("Test case "+testCaseNumber+" has no implementation.");
		}
		LOGGER.info(LOG_END);
		return ResponseEntity.ok().build();
    }

}
