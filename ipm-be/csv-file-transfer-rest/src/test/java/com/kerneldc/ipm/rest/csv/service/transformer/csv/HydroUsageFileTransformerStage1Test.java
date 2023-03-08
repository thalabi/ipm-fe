package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kerneldc.ipm.rest.csv.service.transformer.CsvFileTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.CsvTransformerException;

class HydroUsageFileTransformerStage1Test {
	
	private HydroUsageFileTransformerStage1 hydroUsageFileTransformerStage1 = new HydroUsageFileTransformerStage1();

	private FileProcessingContext context;
	@BeforeEach
	void init() {
		context = new FileProcessingContext(null);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"ChartExportFile2016.csv"})
	void testGoodFile(String csvResourceName) throws URISyntaxException, IOException, AbortFileProcessingException {
		
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);
		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());
		
		hydroUsageFileTransformerStage1.transform(context);
		var resultCsvBufferedReader = Files.newBufferedReader(context.getWorkInProgressFile());

		var expectedYear = "\"2016\"";
		String line;
		int lineCount = 0;
		while ((line = resultCsvBufferedReader.readLine()) != null) {
			if (lineCount == 0) {
				System.out.println("testGoodFile: "+line);
				assertThat("Expected line to start with "+CsvFileTransformerService.SOURCE_CSV_LINE_NUMBER, line.startsWith("\""+CsvFileTransformerService.SOURCE_CSV_LINE_NUMBER+"\""), equalTo(true));
				lineCount++;
				continue;
			}
			System.out.println("testGoodFile: "+line);
			var cells = line.split(",");
			assertThat(cells[1], equalTo(expectedYear));
			lineCount++;
		}
		
		var expectedLineCount = getLineCount(csvResourceName) - 1;
		assertThat("Line counts do not match", lineCount, equalTo(expectedLineCount));
	}
	@Test
	void testBadHeader() throws URISyntaxException, AbortFileProcessingException {
		var csvResourceName = "ChartExportFile2017-bad-header.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);
		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());

		AbortFileProcessingException abortFileProcessingException = assertThrows(AbortFileProcessingException.class,
				() -> hydroUsageFileTransformerStage1.transform(context));
		assertThat("Exception message not as expected", abortFileProcessingException.getMessage(), equalTo(HydroUsageFileTransformerStage1.BAD_HEADER_MESSAGE));
	}

	@Test
	void testBadSecondHeader() throws URISyntaxException, CsvTransformerException, AbortFileProcessingException, IOException {
		var csvResourceName = "ChartExportFile2017-bad-second-header.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);
		
		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());

		AbortFileProcessingException abortFileProcessingException = assertThrows(AbortFileProcessingException.class,
				() -> hydroUsageFileTransformerStage1.transform(context));
		assertThat("Exception message not as expected", abortFileProcessingException.getMessage(), equalTo(HydroUsageFileTransformerStage1.BAD_SECOND_HEADER_MESSAGE));
	}

	private int getLineCount(String resourceName) throws IOException, URISyntaxException {
		return Files.readAllLines(Path.of(getClass().getClassLoader().getResource(resourceName).toURI())).size();
	}
}
