package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.SunshineList;
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
public class SunshineListTransformerStage1 implements ICsvFileTransformer {
	
	protected static final String TRANSFORMER_NAME = "SunshineListTransformerStage1";

	/**
	 * This transformer will,
	 * 1) remove all non printable characters from the first line 
	 * 2) rename the column title 'Position' to 'Job Title' 
	 * 3) strip salary and benefits amount of $ and ,
	 * @throws AbortFileProcessingException
	 */
	@Override
	public void transform(FileProcessingContext context) throws AbortFileProcessingException {

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
		LOGGER.info("outputFilePath: {}", outputFilePath);

		try (var csvReader = new CSVReader(new BufferedReader(new FileReader(inputFilePath.toFile())));
				var csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(outputFilePath.toFile())));) {
        	
			String[] cells;
			var lineNumber = 0;

			try {
				while ((cells = csvReader.readNext()) != null) {
					if (++lineNumber == 1) {
						
						removeNonPrintableCharacters(cells);
						
						transformHeaderTitles(cells);
						
						csvWriter.writeNext(cells);
						continue; // skip parsing header
					}
					
					cells[SunshineList.SALARY_INDEX+1] = stripAmount(cells[SunshineList.SALARY_INDEX+1]);
					cells[SunshineList.BENEFITS_INDEX+1] = stripAmount(cells[SunshineList.BENEFITS_INDEX+1]);

					csvWriter.writeNext(cells);
				}
			} catch (CsvValidationException e) {
				e.printStackTrace();
				throw new AbortFileProcessingException(getTransformerName(), e);
			}

        	
		} catch (IOException e) {
			throw new AbortFileProcessingException(getTransformerName(), e);
		} 
	}

	private void removeNonPrintableCharacters(String[] cells) {
		for (int i=0; i<cells.length; i++) {
			if (! /* not */ StringUtils.isAsciiPrintable(cells[i])) {
			cells[i] = cells[i].replaceAll("[^\\p{ASCII}]", ""); // strip any non-ascii characters
			}
		}
		
	}

	private void transformHeaderTitles(String[] headerTitles) {
		for (int i=0; i<headerTitles.length; i++) {
			if (StringUtils.equalsAnyIgnoreCase(headerTitles[i], SunshineList.ALTERNATE_JOB_TITLE)) {
				headerTitles[i] = SunshineList.JOB_TITLE;
			}
			if (StringUtils.equalsAnyIgnoreCase(headerTitles[i], SunshineList.ALTERNATE_LAST_NAME)) {
				headerTitles[i] = SunshineList.LAST_NAME;
			}
			if (StringUtils.equalsAnyIgnoreCase(headerTitles[i], SunshineList.ALTERNATE_CALENDAR_YEAR)) {
				headerTitles[i] = SunshineList.CALENDAR_YEAR;
			}
			if (StringUtils.equalsAnyIgnoreCase(headerTitles[i], SunshineList.ALTERNATE_BENEFITS)) {
				headerTitles[i] = SunshineList.BENEFITS;
			}
			if (StringUtils.equalsAnyIgnoreCase(headerTitles[i], SunshineList.ALTERNATE_SALARY)) {
				headerTitles[i] = SunshineList.SALARY;
			}
		}
	}

	/**
	 * Removes all $ and , from string
	 * @param amount
	 * @return
	 */
	private String stripAmount(String amount) {
		amount = amount.replace("$", "");
		amount = amount.replace(",", "");
		if (StringUtils.equals(amount, "-")) {
			amount = "";
		}
		return amount;
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(UploadTableEnum.SUNSHINE_LIST)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}

	@Override
	public String getTransformerName() {
		return TRANSFORMER_NAME;
	}

}
