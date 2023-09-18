package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.util.AppFileUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HydroUsageFileTransformerStage2 implements ICsvFileTransformer {

	@Override
	public void transform(FileProcessingContext context)
			throws AbortFileProcessingException {
		
		//var context = FileProcessingContext.get();
		
		Path inputFilePath = null;
		Path outputFilePath = null;
		try {
			inputFilePath = AppFileUtils.createTempFile();
			Files.copy(context.getWorkInProgressFile(), inputFilePath, StandardCopyOption.REPLACE_EXISTING);
			outputFilePath = AppFileUtils.createTempFile();
		} catch (IOException e) {
			throw new AbortFileProcessingException(getTransformerName(), e);
		}
		context.setWorkInProgressFile(outputFilePath);
		
		try (var csvReader = new CSVReader(new BufferedReader(new FileReader(inputFilePath.toFile())));
				var csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(outputFilePath.toFile())));) {
        	
			String[] cells;
			while ((cells = csvReader.readNext()) != null) {
        		LOGGER.debug("Line: {}", String.join(", ", cells));
            	csvWriter.writeNext(cells);
			}
			// write last line again to cause a duplicate logical key
        	//csvWriter.writeNext(lastCells);
        	
		} catch (IOException | CsvValidationException e) {
			throw new AbortFileProcessingException(getTransformerName(), e);
		}

		//return csvFileTransformerResult.withTransformedFileAndCsvTransformerExceptionList(outputFilePath, csvFileTransformerResult.csvTransformerExceptionList());
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {

		return uploadTableEnum.equals(UploadTableEnum.HYDRO_USAGE)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_TWO);
	}

	@Override
	public String getTransformerName() {
		return "HydroUsageFileTransformerStage2";
	}

}
