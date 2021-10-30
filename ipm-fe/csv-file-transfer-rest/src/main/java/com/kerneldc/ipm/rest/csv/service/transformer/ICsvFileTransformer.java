package com.kerneldc.ipm.rest.csv.service.transformer;

import java.io.BufferedReader;

import com.kerneldc.common.enums.IEntityEnum;

public interface ICsvFileTransformer {

	BufferedReader transform(BufferedReader bufferedReader) throws TransformerException;
	
	boolean canHandle(IEntityEnum entityEnum, TransformationStagesEnum transformationStagesEnum);
}
