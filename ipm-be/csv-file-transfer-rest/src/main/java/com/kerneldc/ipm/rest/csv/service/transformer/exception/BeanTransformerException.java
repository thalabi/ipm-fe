package com.kerneldc.ipm.rest.csv.service.transformer.exception;

import java.util.StringJoiner;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.ipm.rest.csv.service.transformer.bean.IBeanTransformer;

import lombok.Getter;
import lombok.Setter;


/**
 * Transformer exception used to capture bean transformers {@link IBeanTransformer} exceptions
 *
 */
public class BeanTransformerException extends Exception {
	private static final long serialVersionUID = 1L;

	@Getter @Setter
	private String transformerName;

	@Getter @Setter
	private AbstractPersistableEntity bean;

	//public BeanTransformerException(String transformerName, long lineNumber, String bean, String message) {
	public BeanTransformerException(String transformerName, AbstractPersistableEntity bean, String message) {
		super(message);
		this.transformerName = transformerName;
//		this.lineNumber = lineNumber;
		this.bean = bean;
	}

	public BeanTransformerException(String transformerName, AbstractPersistableEntity bean, String message, Throwable cause) {
		super(message, cause);
		this.transformerName = transformerName;
//		this.lineNumber = lineNumber;
		this.bean = bean;
	}


	public BeanTransformerException(String transformerName, String message, Throwable cause) {
		super(message, cause);
		this.transformerName = transformerName;
//		this.lineNumber = -1;
		this.bean = null;
	}

	public BeanTransformerException(String transformerName, Throwable cause) {
		super(cause);
		this.transformerName = transformerName;
//		this.lineNumber = -1;
		this.bean = null;
	}

	public BeanTransformerException(String transformerName, String message) {
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
