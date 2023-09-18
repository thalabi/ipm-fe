package com.kerneldc.common.exception;

import org.hibernate.JDBCException;
import org.springframework.dao.DataIntegrityViolationException;

import lombok.Getter;

public class RecordIntegrityViolationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String constraintMessage;
	
	public RecordIntegrityViolationException(DataIntegrityViolationException cause) {
		super("Operation caused data integrity violation.", cause);
		var jdbcException = (JDBCException)cause.getCause();
		var sqlException = jdbcException.getSQLException();
		constraintMessage = sqlException.getMessage();
	}
	
//	public String getStackTraceString() {
//		return ExceptionUtils.getStackTrace(getCause());
//	}
//	public record Content(String message, String detailMessage, String stackTraceString) {}
//	public Content getContent() {
//		return new Content(getMessage(), this.constraintMessage, getStackTraceString());
//	}

//	public RecordIntegrityViolationException() {
//		// TODO Auto-generated constructor stub
//	}
//
//	public RecordIntegrityViolationException(String message) {
//		super(message);
//		// TODO Auto-generated constructor stub
//	}
	
//	public RecordIntegrityViolationException(String message, Throwable cause) {
//		super(message, cause);
//		// TODO Auto-generated constructor stub
//	}
//
//	public RecordIntegrityViolationException(String message, Throwable cause, boolean enableSuppression,
//			boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//		// TODO Auto-generated constructor stub
//	}

}
