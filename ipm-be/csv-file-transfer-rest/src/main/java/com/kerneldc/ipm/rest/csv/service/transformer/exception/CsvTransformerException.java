package com.kerneldc.ipm.rest.csv.service.transformer.exception;

import java.util.StringJoiner;

import com.kerneldc.ipm.rest.csv.service.transformer.csv.ICsvFileTransformer;

import lombok.Getter;
import lombok.Setter;


/**
 * Transformer exception used to capture csv transformers {@link ICsvFileTransformer} exceptions
 *
 */
public class CsvTransformerException extends Exception {
	private static final long serialVersionUID = 1L;

	@Getter @Setter
	private String transformerName;

	@Getter @Setter
	private String[] cells = new String[0];
	@Getter @Setter
	private long lineNumber = 0;

	public CsvTransformerException(String transformerName, String[] cells, long lineNumber, String message) {
		super(message);
		this.transformerName = transformerName;
		this.cells = cells;
		this.lineNumber = lineNumber;
	}

	public CsvTransformerException(String transformerName, Throwable cause) {
		super(cause);
		this.transformerName = transformerName;
	}

	public CsvTransformerException(String transformerName, String message) {
		super(message);
		this.transformerName = transformerName;
	}
	

	@Override
	public String toString() {
		var string = new StringJoiner(",")
				.add("transformerName=" + getTransformerName())
				.add("lineNumber=" + String.valueOf(getLineNumber()))
				//.add("bean=" + getBean())
				.add("message=" + getMessage());
		if (getCause() != null) {
			string.add("cause=" + getCause().getMessage());
		}
		return string.toString();
	}
}
