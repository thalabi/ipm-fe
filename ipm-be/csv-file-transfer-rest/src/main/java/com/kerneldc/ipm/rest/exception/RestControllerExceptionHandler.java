package com.kerneldc.ipm.rest.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.kerneldc.common.exception.ConcurrentRecordAccessException;
import com.kerneldc.common.exception.RecordIntegrityViolationException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class RestControllerExceptionHandler {

	@ExceptionHandler(RecordIntegrityViolationException.class)
	protected ResponseEntity<RecordIntegrityViolationException.Content> handleRecordIntegrityViolationException(RecordIntegrityViolationException ex) {
		var constraintMessage = ex.getConstraintMessage();
		LOGGER.info("constraintMessage: {}", constraintMessage);
		var stackTrace = ex.getStackTraceString();
		LOGGER.info("stackTrace: {}", stackTrace);
		return new ResponseEntity<>(ex.getContent(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConcurrentRecordAccessException.class)
	protected ResponseEntity<String> handleConcurrentRecordAccessException(ConcurrentRecordAccessException ex) {
		//return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	protected ResponseEntity<String> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
		//return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}
	/*
	 *  DataAccessException is root of data access exceptions
	 */
	@ExceptionHandler(DataAccessException.class)
	protected ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
		LOGGER.error("Exception: {}", ExceptionUtils.getStackTrace(ex));
		//return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
