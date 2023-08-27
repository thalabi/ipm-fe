package com.kerneldc.ipm.rest.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.kerneldc.common.exception.ConcurrentRecordAccessException;

@ControllerAdvice
public class RestControllerExceptionHandler {

	@ExceptionHandler(ConcurrentRecordAccessException.class)
	protected ResponseEntity<String> handleConcurrentRecordAccessException(ConcurrentRecordAccessException ex) {
		//return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
	}
	
	/*
	 *  DataAccessException is root of data access exceptions
	 */
	@ExceptionHandler(DataAccessException.class)
	protected ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
		//return new ResponseEntity<>(NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
