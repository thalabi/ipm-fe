package com.kerneldc.ipm.rest.csv.service.transformer;

import java.util.StringJoiner;

import com.kerneldc.common.domain.AbstractPersistableEntity;

import lombok.Getter;
import lombok.Setter;


public class TransformerException extends Exception {
	private static final long serialVersionUID = 1L;

	@Getter @Setter
	private String transformerName;

//	@Getter @Setter
//	private long lineNumber;

	@Getter @Setter
	private AbstractPersistableEntity bean;

	//public TransformerException(String transformerName, long lineNumber, String bean, String message) {
	public TransformerException(String transformerName, AbstractPersistableEntity bean, String message) {
		super(message);
		this.transformerName = transformerName;
//		this.lineNumber = lineNumber;
		this.bean = bean;
	}

	public TransformerException(String transformerName, AbstractPersistableEntity bean, String message, Throwable cause) {
		super(message, cause);
		this.transformerName = transformerName;
//		this.lineNumber = lineNumber;
		this.bean = bean;
	}


	public TransformerException(String transformerName, String message, Throwable cause) {
		super(message, cause);
		this.transformerName = transformerName;
//		this.lineNumber = -1;
		this.bean = null;
	}

	public TransformerException(String transformerName, Throwable cause) {
		super(cause);
		this.transformerName = transformerName;
//		this.lineNumber = -1;
		this.bean = null;
	}

	public TransformerException(String transformerName, String message) {
		super(message);
		this.transformerName = transformerName;
//		this.lineNumber = -1;
		this.bean = null;
	}
	
	@Override
	public String toString() {
		var string = new StringJoiner(",")
				.add("transformerName=" + getTransformerName())
				//.add("lineNumber=" + String.valueOf(getLineNumber()))
				.add("bean=" + getBean())
				.add("message=" + getMessage());
		if (getCause() != null) {
			string.add("cause=" + getCause().getMessage());
		}
		return string.toString();
	}
}
