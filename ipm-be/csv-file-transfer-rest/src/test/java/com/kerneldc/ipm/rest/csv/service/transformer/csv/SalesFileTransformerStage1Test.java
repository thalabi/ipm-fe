package com.kerneldc.ipm.rest.csv.service.transformer.csv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;

class SalesFileTransformerStage1Test {
	
	private SalesFileTransformerStage1 salesFileTransformerStage1 = new SalesFileTransformerStage1();

	private FileProcessingContext context;
	@BeforeEach
	void init() {
		context = new FileProcessingContext(null);
	}

	@ParameterizedTest
	@ValueSource(strings = {"sales-6-data-lines-good-file.csv", "sales-5-lines-good-file-different-column-order.csv"})
	void testGoodFile(String csvResourceName) throws AbortFileProcessingException, URISyntaxException, IOException {
		
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);

		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());
		
		salesFileTransformerStage1.transform(context);
		var resultCsvBufferedReader = Files.newBufferedReader(context.getWorkInProgressFile());
		
		@SuppressWarnings("unused")
		String line;
		int lineCount = 0;
		while ((line = resultCsvBufferedReader.readLine()) != null) {
			lineCount++;
		}
		
		var expectedLineCount = getLineCount(csvResourceName);
		assertThat("Line counts do not match", lineCount, equalTo(expectedLineCount));
	}
	@Test
	void testWrongNumberOfHeaderNames() throws AbortFileProcessingException, URISyntaxException {
		var csvResourceName = "sales-wrong-number-of-header-names.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);
		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());
		
		salesFileTransformerStage1.transform(context);

		var exceptionList = context.getCsvTransformerExceptionList();
		assertThat("Expected size of exception list to be 1", exceptionList.size(), equalTo(1));
		
		var exception = exceptionList.get(0);
		var exceptionMessageStrippedOfParameters = exception.getMessage().replaceAll("\\[.*?\\]", StringUtils.EMPTY);
		var expectedDxceptionMessageStrippedOfParameters = SalesFileTransformerStage1.headerValidateMessageTemplate1.replaceAll("\\[.*?\\]", StringUtils.EMPTY);
		assertThat("Exception message not as expected", exceptionMessageStrippedOfParameters, equalTo(expectedDxceptionMessageStrippedOfParameters));
		assertThat("Transformer name not as expected", exception.getTransformerName(), equalTo(salesFileTransformerStage1.getTransformerName()));
	}

	@Test
	void testWrongHeaderNames_throwsException() throws AbortFileProcessingException, URISyntaxException {
		var csvResourceName = "sales-wrong-header-names.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);
		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());

		salesFileTransformerStage1.transform(context);
		
		var exceptionList = context.getCsvTransformerExceptionList();
		assertThat("Expected size of exception list to be 1", exceptionList.size(), equalTo(1));
		
		var exception = exceptionList.get(0);
		var exceptionMessageStrippedOfParameters = exception.getMessage().replaceAll("\\[.*?\\]", StringUtils.EMPTY);
		var expectedDxceptionMessageStrippedOfParameters = SalesFileTransformerStage1.headerValidateMessageTemplate2.replaceAll("\\[.*?\\]", StringUtils.EMPTY);
		assertThat("Exception message not as expected", exceptionMessageStrippedOfParameters, equalTo(expectedDxceptionMessageStrippedOfParameters));
		assertThat("Transformer name not as expected", exception.getTransformerName(), equalTo(salesFileTransformerStage1.getTransformerName()));
	}

	@Test
	void testWrongHeaderNames_transformedCsvFile_isNull() throws IOException, URISyntaxException, AbortFileProcessingException {
		var csvResourceName = "sales-wrong-header-names.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);
		//var csvFileTransformerResult = new CsvFileTransformerResult(csvFile, new ArrayList<CsvTransformerException>());

		salesFileTransformerStage1.transform(context);
		assertThat("Expected result file to be empty", context.getWorkInProgressFile().toFile().length(), equalTo(0l));
	}
	
	@Test
	void testInvalidCountry_throwsException() throws IOException, URISyntaxException, AbortFileProcessingException {
		var csvResourceName = "sales-invalid-country.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());
		context.setWorkInProgressFile(csvFile);

		salesFileTransformerStage1.transform(context);
		assertThat("Expected one exception", context.getCsvTransformerExceptionList().size(), equalTo(1));
		assertThat("Unexpected exception message", context.getCsvTransformerExceptionList().get(0).getMessage(), equalTo(SalesFileTransformerStage1.INVALID_COUNTRY_MESSAGE));
		assertThat("Unexpected transformer name", context.getCsvTransformerExceptionList().get(0).getTransformerName(), equalTo(SalesFileTransformerStage1.TRANSFORMER_NAME));
	}
	
	private int getLineCount(String resourceName) throws IOException, URISyntaxException {
		return Files.readAllLines(Path.of(getClass().getClassLoader().getResource(resourceName).toURI())).size();
	}
}
