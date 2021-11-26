package com.kerneldc.ipm.rest.csv.service.transformer;

import java.io.BufferedReader;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.kerneldc.common.enums.IEntityEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvFileTransformerService {

	private final Collection<ICsvFileTransformer> csvFileTransformerCollection;
	
	public record CsvFileTransformerResult(BufferedReader csvBufferedReader, TransformerException transformerException) {}
	
	public CsvFileTransformerResult applyTransformers(IEntityEnum uploadTabelEnum, BufferedReader csvBufferedReader) {
		
		for (ICsvFileTransformer transformer: csvFileTransformerCollection) {
			for (TransformationStageEnum stage : TransformationStageEnum.values())
				if (transformer.canHandle(uploadTabelEnum, stage)) {
					try {
						LOGGER.info("Using {} to transform csv file for {} table.", transformer.getClass().getSimpleName(), uploadTabelEnum);
						csvBufferedReader = transformer.transform(csvBufferedReader);
					} catch (TransformerException e) {
						return new CsvFileTransformerResult(null, e);
					}
				}
		}
		return new CsvFileTransformerResult(csvBufferedReader, null);
	}

}
