package com.kerneldc.common.exception;

public class RecordIntegrityViolationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

//	public RecordIntegrityViolationException() {
//		// TODO Auto-generated constructor stub
//	}
//
//	public RecordIntegrityViolationException(String message) {
//		super(message);
//		// TODO Auto-generated constructor stub
//	}

	public RecordIntegrityViolationException(Throwable cause) {
		super("Operation caused data integrity violation.", cause);
	}

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
