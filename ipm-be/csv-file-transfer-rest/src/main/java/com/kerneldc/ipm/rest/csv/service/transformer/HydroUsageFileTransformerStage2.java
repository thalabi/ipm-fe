package com.kerneldc.ipm.rest.csv.service.transformer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HydroUsageFileTransformerStage2 implements ICsvFileTransformer {

	@Override
	public BufferedReader transform(BufferedReader bufferedReader) throws TransformerException {
    	var out = new ByteArrayOutputStream();
        
		try (var csvReader = new CSVReader(bufferedReader);
				var csvWriter = new CSVWriter(new OutputStreamWriter(out));) {
        	
			String[] cells;
			var lastCells = new String[0];
			while ((cells = csvReader.readNext()) != null) {
        		LOGGER.debug("Line: {}", String.join(", ", cells));
            	csvWriter.writeNext(cells);
            	lastCells = cells;
			}
			// write last line again to cause a duplicate logical key
        	//csvWriter.writeNext(lastCells);
        	
		} catch (CsvValidationException | IOException e) {
			throw new TransformerException(StringUtils.EMPTY, e);
    		
    	}
        
        byte[] byteArray = out.toByteArray();
        try {
			out.close();
		} catch (IOException e) {
			throw new TransformerException("Unable to close ByteArrayOutputStream", e);
		}
    	
    	return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArray)));
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(UploadTableEnum.HYDRO_USAGE)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_TWO);
	}
}
