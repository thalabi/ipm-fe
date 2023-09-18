package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.CsvFileTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.CsvTransformerException;
import com.kerneldc.ipm.util.AppFileUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HydroUsageFileTransformerStage1 implements ICsvFileTransformer {

	public static final String FIRST_LINE_EMPTY_MESSAGE = "First line is empty";
	public static final String SECOND_LINE_EMPTY_MESSAGE = "Second line is empty";
	public static final String BAD_HEADER_MESSAGE = "Header does not match expected pattern \'Usage for the period of yyyy\'";
	private static final String EXPECTED_SECOND_HEADER_LINE = "1,Months,HighTemp,LowTemp,Off-Peak,Mid-Peak,On-Peak";
	public static final String BAD_SECOND_HEADER_MESSAGE = "Second line does not match \'"+EXPECTED_SECOND_HEADER_LINE+"\'";
	
	@Override
	public void transform(FileProcessingContext context) throws AbortFileProcessingException {

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
		
        var lineNumber = 0l;
        
		try (var csvReader = new CSVReader(new BufferedReader(new FileReader(inputFilePath.toFile())));
				var csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(outputFilePath.toFile())));) {
        	// read first line and extract year
            String[] cells;
			try {
				cells = csvReader.readNext();
			} catch (CsvValidationException e) {
				throw new AbortFileProcessingException(getTransformerName(), e);
			}
            lineNumber++;

        	if (cells.length == 0) {
        		throw new AbortFileProcessingException(getTransformerName(), FIRST_LINE_EMPTY_MESSAGE);
        	}
    		LOGGER.debug("Line: {}", String.join(", ", cells));
    		
        	var p = Pattern.compile(".*Usage for the period of (\\d{4})"); // Add '.*' at the beginning of regex. There is a bug in the received file where the first line is prefixed by a non printable character
        	var m = p.matcher(cells[1]);
        	
        	if (! /* not */ m.matches()) {
        		throw new AbortFileProcessingException(getTransformerName(), BAD_HEADER_MESSAGE);
        	}
        	var year = m.group(1);
        	LOGGER.debug("year: {}", year);

        	// read second line with header 'Months	HighTemp	LowTemp	Off-Peak	Mid-Peak	On-Peak' and replace with 'Year Month	HighTemp	LowTemp	Off-Peak	Mid-Peak	On-Peak' 
			try {
				cells = csvReader.readNext();
			} catch (CsvValidationException e) {
				throw new AbortFileProcessingException(getTransformerName(), e);
			}
            lineNumber++;

        	var sourceFileSecondHeader = cells;
        	
        	LOGGER.debug("Line: {}", String.join(", ", cells));
        	if (cells.length == 0) {
        		throw new AbortFileProcessingException(getTransformerName(), SECOND_LINE_EMPTY_MESSAGE);
        	}
        	// check that second line is equal to 'Months,HighTemp,LowTemp,Off-Peak,Mid-Peak,On-Peak'
        	if (! /* not */ String.join(",", cells).equals(EXPECTED_SECOND_HEADER_LINE)) {
        		throw new AbortFileProcessingException(getTransformerName(), BAD_SECOND_HEADER_MESSAGE);
        	}
        	
        	// write the new header as 'sourceCsvLineNumber,Year,Month,HighTemp,LowTemp,Off-Peak,Mid-Peak,On-Peak'
        	var newHeader = new String[cells.length+1];
        	newHeader[0] = CsvFileTransformerService.SOURCE_CSV_LINE_NUMBER;
        	newHeader[1] = "Year";
        	newHeader[2] = "Month";
        	for (var i=2; i<cells.length; i++) {
        		newHeader[i+1] = cells[i];
        	}
        	csvWriter.writeNext(newHeader);
        	LOGGER.debug("Output line: {}", String.join(", ", newHeader));
        	
        	// read the rest of the file and include the year and month
        	
        	while ((cells = csvReader.readNext()) != null) {
                lineNumber++;
			   // nextLine[] is an array of values from the line
        		LOGGER.debug("Line: {}", String.join(", ", cells));
        		var outputCells = new String[cells.length+1];	
        		outputCells[0] = String.valueOf(Integer.parseInt(cells[0])-1);
        		outputCells[1] = year;
        		// A trivial validator for testing
        		validateOffPeak(sourceFileSecondHeader, cells, lineNumber);
            	for (int i=1; i<cells.length; i++) {
            		outputCells[i+1] = cells[i];
            	}
            	csvWriter.writeNext(outputCells);
            	LOGGER.debug("Output line: {}", String.join(", ", outputCells));
			}
        	
		} catch (CsvTransformerException e) {
			context.getCsvTransformerExceptionList().add(e);
    		
		} catch (IOException | CsvValidationException e) {
			throw new AbortFileProcessingException(getTransformerName(), e);
		}
        
		//return csvFileTransformerResult.withTransformedFileAndCsvTransformerExceptionList(outputFilePath, csvFileTransformerResult.csvTransformerExceptionList());
	}

	private void validateOffPeak(String[] sourceFileSecondHeader, String[] cells, long lineNumber) throws CsvTransformerException {
		var indexOfOffPeakColumn = Arrays.asList(sourceFileSecondHeader).indexOf("Off-Peak");
		if (indexOfOffPeakColumn == -1) {
			LOGGER.warn("Could not search for Off-Peak column name in header, ignoring off peak validator");
			return;
		}
		if (Float.parseFloat(cells[indexOfOffPeakColumn]) < 0) {
			throw new CsvTransformerException(getTransformerName(), cells, lineNumber, "Off-Peak value of " + cells[indexOfOffPeakColumn] + " cannot be negative"); 
		}
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(UploadTableEnum.HYDRO_USAGE)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}

	@Override
	public String getTransformerName() {
		return"HydroUsageFileTransformerStage1";
	}

}
