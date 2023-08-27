package com.kerneldc.common.exception;

public class ConcurrentRecordAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

//	public ConcurrentRecordAccessException() {
//	}
//
//	public ConcurrentRecordAccessException(String message) {
//		super(message);
//	}

	public ConcurrentRecordAccessException(Throwable cause) {
		super("Record was updated or deleted by someone else. Please try again.", cause);
	}

//	public ConcurrentRecordAccessException(String message, Throwable cause) {
//		super(message, cause);
//	}
//
//	public ConcurrentRecordAccessException(String message, Throwable cause, boolean enableSuppression,
//			boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

}
