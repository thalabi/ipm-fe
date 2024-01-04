package com.kerneldc.ipm.rest.csv.service.transformer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.kerneldc.ipm.rest.csv.service.transformer.csv.ICsvFileTransformer;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.util.AppFileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvFileTransformerService {

	public static final String SOURCE_CSV_LINE_NUMBER = "sourceCsvLineNumber";
	private final Collection<ICsvFileTransformer> csvFileTransformerCollection;
		
	public void applyTransformers(InputStream inputStream, FileProcessingContext context) throws IOException, AbortFileProcessingException {
		
		var pathAndLineCount = copyToFilePrefixingLineNumber(inputStream);
		context.setWorkInProgressFile(pathAndLineCount.filePath());
		context.setSourceCsvDataRowsCount(pathAndLineCount.lineCount());

		for (ICsvFileTransformer transformer: csvFileTransformerCollection) {
			for (TransformationStageEnum stage : TransformationStageEnum.values())
				if (transformer.canHandle(context.getUploadTableEnum(), stage)) {
						LOGGER.info("Applying {} to transform csv file for {} table.", transformer.getTransformerName(), context.getUploadTableEnum());
						transformer.transform(context);
						LOGGER.info("transformedFile: {}", context.getWorkInProgressFile().toString());
				}
		}
	}
	
	public record PathAndLineCount(Path filePath, long lineCount) {}
	/**
	 * Copies the contents of input stream to a temp file,
	 * adds and sets the sourceCsvLineNumber column to the line number in the source csv file
	 * 
	 * @param inputStream
	 * @return returns the path of the temp file and the line count of data lines
	 * @throws IOException
	 */
	private PathAndLineCount copyToFilePrefixingLineNumber(InputStream inputStream) throws IOException {
		var path = AppFileUtils.createTempFile();
		long lineNumber = 0;
		try (var bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				var bufferedWriter = new BufferedWriter(new FileWriter(path.toFile()));) {
			while (bufferedReader.ready()) {
				lineNumber++;
				var inputLine = bufferedReader.readLine();
				if (lineNumber == 1) {
					bufferedWriter.write(SOURCE_CSV_LINE_NUMBER + "," + inputLine);
				} else {
					bufferedWriter.write(lineNumber + "," + inputLine);
				}
				bufferedWriter.newLine();
			}
		}
		return new PathAndLineCount(path, lineNumber);
	}
}
