package com.kerneldc.common.exception;

public class ConcurrentRecordAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public static final String UPDATE_EXCEPTION_MESSAGE = "Record was updated or deleted by someone else.";
	public static final String DELETE_EXCEPTION_MESSAGE = "Record was deleted by someone else.";

//	public ConcurrentRecordAccessException() {
//	}
//
//	public ConcurrentRecordAccessException(String message) {
//		super(message);
//	}

//	public ConcurrentRecordAccessException(Throwable cause) {
//		super("Record was updated or deleted by someone else. Please try again.", cause);
//	}

	public ConcurrentRecordAccessException(String message, Throwable cause) {
		super(message, cause);
	}
//
//	public ConcurrentRecordAccessException(String message, Throwable cause, boolean enableSuppression,
//			boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

}
