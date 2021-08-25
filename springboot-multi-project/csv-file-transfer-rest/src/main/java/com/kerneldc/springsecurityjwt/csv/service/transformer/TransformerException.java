package com.kerneldc.springsecurityjwt.csv.service.transformer;

public class TransformerException extends Exception {
	private static final long serialVersionUID = 1L;

	public TransformerException(String message) {
		super(message);
	}

	public TransformerException(String message, Throwable cause) {
		super(message, cause);
	}

}
