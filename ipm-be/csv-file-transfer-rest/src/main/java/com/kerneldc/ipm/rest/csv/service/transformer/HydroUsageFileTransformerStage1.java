package com.kerneldc.ipm.rest.csv.service.transformer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HydroUsageFileTransformerStage1 implements ICsvFileTransformer {

	private static final String EXPECTED_SECOND_HEADER_LINE = "Months,HighTemp,LowTemp,Off-Peak,Mid-Peak,On-Peak";
	
	@Override
	public BufferedReader transform(BufferedReader bufferedReader) throws TransformerException {
    	var out = new ByteArrayOutputStream();
        
		try (var csvReader = new CSVReader(bufferedReader);
				var csvWriter = new CSVWriter(new OutputStreamWriter(out));) {
        	// read first line and extract year
            var cells = csvReader.readNext();

        	if (cells.length == 0) {
        		throw new TransformerException(getTransformerName(), "First line is empty");
        	}
    		LOGGER.debug("Line: {}", String.join(", ", cells));
        	var p = Pattern.compile(".*Usage for the period of (\\d{4})"); // Add '.*' at the beginning of regex. There is a bug in the received file where the first line is prefixed by a non printable character
        	var m = p.matcher(cells[0]);
        	
        	if (! /* not */ m.matches()) {
        		throw new TransformerException(getTransformerName(), "Header does not match expected pattern \'Usage for the period of yyyy\'");
        	}
        	var year = m.group(1);
        	LOGGER.debug("year: {}", year);

        	// read header 'Months	HighTemp	LowTemp	Off-Peak	Mid-Peak	On-Peak' and replace with 'Year Month	HighTemp	LowTemp	Off-Peak	Mid-Peak	On-Peak' 
        	cells = csvReader.readNext();

        	LOGGER.debug("Line: {}", String.join(", ", cells));
        	if (cells.length == 0) {
        		throw new TransformerException(getTransformerName(), "Second line is empty");
        	}
        	// check that second line is equal to 'Months,HighTemp,LowTemp,Off-Peak,Mid-Peak,On-Peak'
        	if (! /* not */ String.join(",", cells).equals(EXPECTED_SECOND_HEADER_LINE)) {
        		throw new TransformerException(getTransformerName(), "Second line does not match \'"+EXPECTED_SECOND_HEADER_LINE+"\'");
        	}
        	
        	// write the new header as 'Year,Month,HighTemp,LowTemp,Off-Peak,Mid-Peak,On-Peak'
        	var newHeader = new String[cells.length+1];
        	newHeader[0] = "Year";
        	newHeader[1] = "Month";
        	for (var i=1; i<cells.length; i++) {
        		newHeader[i+1] = cells[i];
        	}
        	csvWriter.writeNext(newHeader);
        	LOGGER.debug("Output line: {}", String.join(", ", newHeader));
        	
        	// read the rest of the file and include the year and month
        	
        	while ((cells = csvReader.readNext()) != null) {
			   // nextLine[] is an array of values from the line
        		LOGGER.debug("Line: {}", String.join(", ", cells));
        		var outputCells = new String[cells.length+1];	
        		outputCells[0] = year;
            	for (int i=0; i<cells.length; i++) {
            		outputCells[i+1] = cells[i];
            	}
            	csvWriter.writeNext(outputCells);
            	LOGGER.debug("Output line: {}", String.join(", ", outputCells));
			}
        	
		} catch (CsvValidationException | IOException e) {
			throw new TransformerException(getTransformerName(), e);
    		
    	}
        
        byte[] byteArray = out.toByteArray();
        try {
			out.close();
		} catch (IOException e) {
			throw new TransformerException(getTransformerName(), "Unable to close ByteArrayOutputStream", e);
		}
        
        LOGGER.debug("Transformed data: {}", new String(byteArray));
    	return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArray)));
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
