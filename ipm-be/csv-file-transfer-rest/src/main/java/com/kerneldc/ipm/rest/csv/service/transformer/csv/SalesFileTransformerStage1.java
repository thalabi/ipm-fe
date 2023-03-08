package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Component;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.rest.csv.service.GenericFileTransferService;
import com.kerneldc.ipm.rest.csv.service.transformer.CsvFileTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.CsvTransformerException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SalesFileTransformerStage1 implements ICsvFileTransformer {
	
	protected static final String headerValidateMessageTemplate1 = "Header name count [%d] does not match required number of [%d]";
	protected static final String headerValidateMessageTemplate2 = "Header names [%s] dont match required header names: [%s]";
	private static final List<String> VALID_COUNTRIES = List.of("canada", "united states");
	protected static final String INVALID_COUNTRY_MESSAGE = "Country is not Canada or United States";
	protected static final String TRANSFORMER_NAME = "SalesFileTransformerStage1";

	/**
	 * This transformer converts the United States to USA in Country column
	 * It does a trivial check to make sure there are no less than 5 United States countries in the file and throws AbortFileProcessingException
	 * It is just used to practice validating dates {@link GenericValidator#isDate()} and writing test units for csv transformers. This is commented out.
	 * @throws AbortFileProcessingException
	 */
	@Override
	public void transform(FileProcessingContext context) throws AbortFileProcessingException {

		//var context = FileProcessingContext.get();
		
		Path inputFilePath = null;
		Path outputFilePath = null;
		try {
			inputFilePath = GenericFileTransferService.createTempFile();
			Files.copy(context.getWorkInProgressFile(), inputFilePath, StandardCopyOption.REPLACE_EXISTING);
			outputFilePath = GenericFileTransferService.createTempFile();
		} catch (IOException e) {
			throw new AbortFileProcessingException(getTransformerName(), e);
		}
		context.setWorkInProgressFile(outputFilePath);
		LOGGER.info("outputFilePath: {}", outputFilePath);

		try (var csvReader = new CSVReader(new BufferedReader(new FileReader(inputFilePath.toFile())));
				var csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(outputFilePath.toFile())));) {
        	
			String[] cells;
			var lineNumber = 0;
			List<String> headerNameList = new ArrayList<String>();
			
			int countryHeaderIndex = 0;
			int countOfUnitedStates = 0;
			
			try {
				while ((cells = csvReader.readNext()) != null) {
					if (++lineNumber == 1) {
						headerNameList = captureHeader(cells);
						csvWriter.writeNext(cells);

						for (int i=0; i<headerNameList.size(); i++) {
							if (StringUtils.equalsIgnoreCase(headerNameList.get(i), "Country")) {
								countryHeaderIndex = i;
								LOGGER.info("countryHeaderIndex: {}", countryHeaderIndex);
								break; // out of loop
							}
						}
						
						continue; // skip parsing header
					}
					
					if (! /* not */ VALID_COUNTRIES.contains(cells[countryHeaderIndex].toLowerCase())) {
						var csvTransformerException = new CsvTransformerException(getTransformerName(), cells, lineNumber, INVALID_COUNTRY_MESSAGE); 
						context.getCsvTransformerExceptionList().add(csvTransformerException);
						continue; // skip line
					}
					if (StringUtils.equalsIgnoreCase(cells[countryHeaderIndex], "united states")) {
						LOGGER.info("cells[{}]: {}", countryHeaderIndex, cells[countryHeaderIndex]);
						cells[countryHeaderIndex] = "USA";
						countOfUnitedStates++;
					}
					
					LOGGER.info("cells: {}",  String.join(", ", cells));

					csvWriter.writeNext(cells);
				}
			} catch (CsvValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} /* while */

			// an example of endProcessExceptionFound being set
			if (countOfUnitedStates < 6) {
				throw new AbortFileProcessingException(getTransformerName(), "There are less than 6 lines with United States");
			}
        	
		} catch (CsvTransformerException e) {
			context.getCsvTransformerExceptionList().add(e);    		
		} catch (IOException e) {
			throw new AbortFileProcessingException(getTransformerName(), e);
		} 
		
    	//return csvFileTransformerResult.withTransformedFileAndCsvTransformerExceptionList(outputFilePath, csvFileTransformerResult.csvTransformerExceptionList());
	}

	private List<String> captureHeader(String[] cells) throws CsvTransformerException {
		return validateHeader(cells);
	}

	private List<String> validateHeader(String[] cells) throws CsvTransformerException {
		var headerNameSet = new HashSet<String>(Arrays.asList(CsvFileTransformerService.SOURCE_CSV_LINE_NUMBER, "Transaction_date","Product","Price","Payment_Type","Name","City","State","Country","Account_Created","Last_Login","Latitude","Longitude","US Zip"));
		if (cells.length != headerNameSet.size()) {
			throw new CsvTransformerException(getTransformerName(), cells, 0, String.format(headerValidateMessageTemplate1, cells.length, headerNameSet.size()));
		}
		var actualHeaderNameSet = new HashSet<String>(Arrays.asList(cells));
		var headerNameSetCopy = new HashSet<String>(headerNameSet);
		headerNameSetCopy.removeAll(actualHeaderNameSet);
		if (CollectionUtils.isNotEmpty(headerNameSetCopy)) {
			throw new CsvTransformerException(getTransformerName(), cells, 0, String.format(headerValidateMessageTemplate2, String.join(", ", cells), String.join(", ", headerNameSet)));
		}
		return Arrays.asList(cells);
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(UploadTableEnum.SALES)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}

	@Override
	public String getTransformerName() {
		return TRANSFORMER_NAME;
	}

}
