package com.kerneldc.ipm.rest.csv.service.enrichment;

public class EnrichmentException extends Exception {
	private static final long serialVersionUID = 1L;

	public EnrichmentException(String message) {
		super(message);
	}

	public EnrichmentException(String message, Throwable cause) {
		super(message, cause);
	}

}
