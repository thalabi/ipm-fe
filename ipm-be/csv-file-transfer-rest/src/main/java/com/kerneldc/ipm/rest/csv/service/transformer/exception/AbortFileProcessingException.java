package com.kerneldc.ipm.rest.csv.service.transformer.exception;

import lombok.Getter;

public class AbortFileProcessingException extends Exception {

	private static final long serialVersionUID = 1L;
	
	@Getter
	private String transformerName;
//	@Getter
//	private List<CsvTransformerException> csvTransformerExceptionList;
//	@Getter
//	private List<CsvException> csvToBeanParseExceptionList;
//	@Getter
//	private List<BeanTransformerException> beanTransformerExceptionList;


//	public interface ListCsvTransformerExceptionRef extends Supplier<List<CsvTransformerException>> {}
//	public AbortFileProcessingException(String transformerName, String message, ListCsvTransformerExceptionRef listCsvTransformerExceptionRef) {
//		super(message);
//		this.transformerName = transformerName;
//		this.csvTransformerExceptionList = listCsvTransformerExceptionRef.get();
//		this.csvToBeanParseExceptionList = List.of();
//		this.beanTransformerExceptionList = List.of();
//	}
//	public AbortFileProcessingException(String transformerName, Throwable cause, ListCsvTransformerExceptionRef listCsvTransformerExceptionRef) {
//		super(cause);
//		this.transformerName = transformerName;
//		this.csvTransformerExceptionList = listCsvTransformerExceptionRef.get();
//		this.csvToBeanParseExceptionList = List.of();
//		this.beanTransformerExceptionList = List.of();
//	}
//	public AbortFileProcessingException(String transformerName, String message, Throwable cause, ListCsvTransformerExceptionRef listCsvTransformerExceptionRef) {
//		super(message, cause);
//		this.transformerName = transformerName;
//		this.csvTransformerExceptionList = listCsvTransformerExceptionRef.get();
//		this.csvToBeanParseExceptionList = List.of();
//		this.beanTransformerExceptionList = List.of();
//	}
//
//	
//	public interface ListCsvToBeanParseExceptionRef extends Supplier<List<CsvException>> {}
//	public AbortFileProcessingException(String transformerName, String message, Throwable cause, ListCsvToBeanParseExceptionRef listCsvToBeanParseExceptionRef) {
//		super(message, cause);
//		this.transformerName = transformerName;
//		this.csvTransformerExceptionList = List.of();
//		this.csvToBeanParseExceptionList = listCsvToBeanParseExceptionRef.get();
//		this.beanTransformerExceptionList = List.of();
//	}
//	
//	public interface ListBeanTransformerExceptionRef extends Supplier<List<BeanTransformerException>> {}
//	public AbortFileProcessingException(String transformerName, String message, Throwable cause, ListBeanTransformerExceptionRef listBeanTransformerExceptionRef) {
//		super(message, cause);
//		this.transformerName = transformerName;
//		this.csvTransformerExceptionList = List.of();
//		this.csvToBeanParseExceptionList = List.of();
//		this.beanTransformerExceptionList = listBeanTransformerExceptionRef.get();
//	}

	public AbortFileProcessingException(String transformerName, String message, Throwable cause) {
		super(message, cause);
		this.transformerName = transformerName;
	}
	public AbortFileProcessingException(String transformerName, String message) {
		super(message);
		this.transformerName = transformerName;
	}
	public AbortFileProcessingException(String transformerName, Throwable cause) {
		super(cause);
		this.transformerName = transformerName;
	}

}
