package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;

public interface ICsvFileTransformer {

	void transform(FileProcessingContext context) throws /*CsvTransformerException, IOException, */AbortFileProcessingException;
	
	boolean canHandle(IEntityEnum entityEnum, TransformationStageEnum transformationStageEnum);

	String getTransformerName();
	
//	static FileProcessingContext getContext() {
//		return FileProcessingContext.get();
//	}
}
