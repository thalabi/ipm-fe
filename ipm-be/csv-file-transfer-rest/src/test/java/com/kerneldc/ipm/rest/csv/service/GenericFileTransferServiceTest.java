package com.kerneldc.ipm.rest.csv.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.kerneldc.common.BaseEntityRepository;
import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.commonservices.repository.EntityRepositoryFactory;
import com.kerneldc.ipm.rest.csv.service.enrichment.BeanReferentialEntityEnrichmentService;
import com.kerneldc.ipm.rest.csv.service.transformer.BeanTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.CsvFileTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.bean.IBeanTransformer;
import com.kerneldc.ipm.rest.csv.service.transformer.bean.SalesBeanTransformerStage1;
import com.kerneldc.ipm.rest.csv.service.transformer.csv.ICsvFileTransformer;
import com.kerneldc.ipm.rest.csv.service.transformer.csv.SalesFileTransformerStage1;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@Slf4j
class GenericFileTransferServiceTest {
	
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

	@MockBean
	private EntityRepositoryFactory<?, ?> entityRepositoryFactory;
	@MockBean
	private CsvFileTransformerService csvFileTransformerService;
	@MockBean
	private BeanTransformerService beanTransformerService;
	@MockBean
	private BeanReferentialEntityEnrichmentService beanReferentialEntityEnrichmentService;
	@MockBean
	private BaseTableRepository<AbstractPersistableEntity, Serializable> tableRepository;
	@MockBean
	private BaseEntityRepository<AbstractEntity, Serializable> entityRepository;

	@InjectMocks
	private GenericFileTransferService genericFileTransferService = new GenericFileTransferService(entityRepositoryFactory, csvFileTransformerService, beanTransformerService, beanReferentialEntityEnrichmentService);
	
	@Test
	void testParse_withEmptyFile_returnsNotNullResults() throws AbortFileProcessingException, URISyntaxException {
		//GenericFileTransferService genericFileTransferService = new GenericFileTransferService(entityRepositoryFactory, csvFileTransformerService, beanTransformerService, beanReferentialEntityEnrichmentService);
		var context = new FileProcessingContext(UploadTableEnum.SALES);
		var csvResourceName = "GenericFileTransferService/emptyWorkInProgressFile.csv";
		var csvFile = Paths.get(getClass().getClassLoader().getResource(csvResourceName).toURI());

		context.setWorkInProgressFile(csvFile);
		genericFileTransferService.parseCsvFileToBeans(context);
		assertAll(
			() -> assertThat("sourceCsvDataRowsCount", context.getSourceCsvDataRowsCount(), is(0l)),
			() -> assertThat("sourceCsvHeaderColumns length", context.getSourceCsvHeaderColumns(), arrayWithSize(0)),
			() -> assertThat("entityColumnNames length", context.getEntityColumnNames(), arrayWithSize(0)),
			() -> assertThat("beans size", context.getBeans(), hasSize(0)),
			() -> assertThat("csvTransformerExceptionList size", context.getCsvTransformerExceptionList(), hasSize(0)),
			() -> assertThat("csvToBeanParseExceptionList size", context.getCsvToBeanParseExceptionList(), hasSize(0)),
			() -> assertThat("beanTransformerExceptionList size", context.getBeanTransformerExceptionList(), hasSize(0)),
			() -> assertThat("persistExceptionsFileLineList size", context.getPersistExceptionsFileLineList(), hasSize(0)));
	}
	
	@Test
	void testParseAndSave_withGoodFile() throws IOException, URISyntaxException {
		var csvResourceName = "GenericFileTransferService/sales-good-file.csv";
		var inputStream = getClass().getClassLoader().getResourceAsStream(csvResourceName);
		
		var fixture = prepareFixture(csvResourceName, List.of(new SalesFileTransformerStage1()), List.of(new SalesBeanTransformerStage1()));
		
		var fileTransferResponse = fixture.parseAndSave(UploadTableEnum.SALES, csvResourceName, inputStream, true);
		LOGGER.info("fileTransferResponse: {}", fileTransferResponse);
		assertAll("fileTransferResponse",
	() -> assertThat("fileTransferResponse", fileTransferResponse, is(notNullValue())),
			() -> assertThat("numberOfLinesInFile", fileTransferResponse.getProcessingStats().getNumberOfLinesInFile(), is(7l)),
			() -> assertThat("numberOfExceptions", fileTransferResponse.getProcessingStats().getNumberOfExceptions(), is(0)),
			() -> assertThat("exceptionsFileName", fileTransferResponse.getExceptionsFileName(), nullValue()),
			() -> assertThat("elapsedTime expected null", fileTransferResponse.getProcessingStats().getElapsedTime(), notNullValue()));
	}


	@Test
	void testParseAndSave_withLessThan6UnitedStates() throws IOException, URISyntaxException {
		var csvResourceName = "GenericFileTransferService/sales-bad-file-less-than-6-lines-with-united-states.csv";
		var numberOfLinesInFile = 7l;
		var numberOfExceptions = 1;
		var inputStream = getClass().getClassLoader().getResourceAsStream(csvResourceName);
		
		var fixture = prepareFixture(csvResourceName, List.of(new SalesFileTransformerStage1()), List.of(new SalesBeanTransformerStage1()));

		var fileTransferResponse = fixture.parseAndSave(UploadTableEnum.SALES, csvResourceName, inputStream, true);
		LOGGER.info("fileTransferResponse: {}", fileTransferResponse);
		
		assertAll("fileTransferResponse",
	() -> assertThat("fileTransferResponse", fileTransferResponse, notNullValue()),
			() -> assertThat("numberOfLinesInFile",fileTransferResponse.getProcessingStats().getNumberOfLinesInFile(), is(numberOfLinesInFile)),
			() -> assertThat("numberOfExceptions", fileTransferResponse.getProcessingStats().getNumberOfExceptions(), is(numberOfExceptions)),
			() -> assertThat("exceptionsFileName", fileTransferResponse.getExceptionsFileName(), matchesPattern(GenericFileTransferService.EXCEPTIONS_FILE_PREFIX+".+"+GenericFileTransferService.EXCEPTIONS_FILE_SUFFIX)),
			() -> assertThat("elapsedTime expected null", fileTransferResponse.getProcessingStats().getElapsedTime(), notNullValue()));
		
		
		var exceptionLines = readExceptionsFileLines(fileTransferResponse.getExceptionsFileName());
		validateExceptionsFileHeaderLines(exceptionLines, csvResourceName, numberOfLinesInFile, numberOfExceptions);

		assertThat("exceptionLines", exceptionLines, hasSize(6)); // 5 for the headers
		
		var exceptionLine = exceptionLines.get(5);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine[0], is("0")),
				() -> assertThat("Error Message", exceptionLine[1], is("There are less than 6 lines with United States")),
				() -> assertThat("Processing Stage", exceptionLine[4], is("SalesFileTransformerStage1")));
	}

	@Test
	void testParseAndSave_withFieldNotNumeric() throws IOException, URISyntaxException {
		var csvResourceName = "GenericFileTransferService/sales-bad-file-field-not-numeric.csv";
		var numberOfLinesInFile = 7l;
		var numberOfExceptions = 1;
		var inputStream = getClass().getClassLoader().getResourceAsStream(csvResourceName);
		
		var fixture = prepareFixture(csvResourceName, List.of(new SalesFileTransformerStage1()), List.of(new SalesBeanTransformerStage1()));
		
		var fileTransferResponse = fixture.parseAndSave(UploadTableEnum.SALES, csvResourceName, inputStream, true);
		LOGGER.info("fileTransferResponse: {}", fileTransferResponse);
		
		assertAll("fileTransferResponse",
	() -> assertThat("fileTransferResponse", fileTransferResponse, notNullValue()),
			() -> assertThat("numberOfLinesInFile",fileTransferResponse.getProcessingStats().getNumberOfLinesInFile(), is(numberOfLinesInFile)),
			() -> assertThat("numberOfExceptions", fileTransferResponse.getProcessingStats().getNumberOfExceptions(), is(numberOfExceptions)),
			() -> assertThat("exceptionsFileName", fileTransferResponse.getExceptionsFileName(), matchesPattern(GenericFileTransferService.EXCEPTIONS_FILE_PREFIX+".+"+GenericFileTransferService.EXCEPTIONS_FILE_SUFFIX)),
			() -> assertThat("elapsedTime expected null", fileTransferResponse.getProcessingStats().getElapsedTime(), notNullValue()));
		
		
		var exceptionLines = readExceptionsFileLines(fileTransferResponse.getExceptionsFileName());
		validateExceptionsFileHeaderLines(exceptionLines, csvResourceName, numberOfLinesInFile, numberOfExceptions);

		assertThat("exceptionLines", exceptionLines, hasSize(6)); // 5 for the headers
		
		var exceptionLine = exceptionLines.get(5);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine[0], is("2")),
				() -> assertThat("Error Message", exceptionLine[1], is("Conversion of XXXX to java.lang.Double failed.")),
				
				() -> assertThat("sourceCsvLineNumber", exceptionLine[2], is("2")),
				() -> assertThat("Transaction_date", exceptionLine[3], is("01/02/2009 04:53")),
				() -> assertThat("Product", exceptionLine[4], is("Product1")),
				() -> assertThat("Price", exceptionLine[5], is("XXXX")),
				() -> assertThat("Payment_Type", exceptionLine[6], is("Visa")),
				() -> assertThat("Name", exceptionLine[7], is("Betina")),
				() -> assertThat("City", exceptionLine[8], is("Parkville")),
				() -> assertThat("State", exceptionLine[9], is("MO")),
				() -> assertThat("Country", exceptionLine[10], is("USA")),
				() -> assertThat("Account_Created", exceptionLine[11], is("1/2/2009 04:42")),
				() -> assertThat("Last_Login", exceptionLine[12], is("1/2/2009 07:49")),
				() -> assertThat("Latitude", exceptionLine[13], is("39.195")),
				() -> assertThat("Longitude", exceptionLine[14], is("-94.68194")),
				() -> assertThat("US Zip", exceptionLine[15], is("64152")),
				
				() -> assertThat("Processing Stage", exceptionLine[17], is("CSV file to beans(opencsv)")));
	}
	
	@Test
	void testParseAndSave_withInvalidCountry() throws IOException, URISyntaxException {
		var csvResourceName = "GenericFileTransferService/sales-bad-file-inavlid-country.csv";
		var numberOfLinesInFile = 8l;
		var numberOfExceptions = 1;
		var inputStream = getClass().getClassLoader().getResourceAsStream(csvResourceName);
		
		var fixture = prepareFixture(csvResourceName, List.of(new SalesFileTransformerStage1()), List.of(new SalesBeanTransformerStage1()));
		
		var fileTransferResponse = fixture.parseAndSave(UploadTableEnum.SALES, csvResourceName, inputStream, true);
		LOGGER.info("fileTransferResponse: {}", fileTransferResponse);
		
		assertAll("fileTransferResponse",
				() -> assertThat("fileTransferResponse", fileTransferResponse, notNullValue()),
				() -> assertThat("numberOfLinesInFile",fileTransferResponse.getProcessingStats().getNumberOfLinesInFile(), is(numberOfLinesInFile)),
				() -> assertThat("numberOfExceptions", fileTransferResponse.getProcessingStats().getNumberOfExceptions(), is(numberOfExceptions)),
				() -> assertThat("exceptionsFileName", fileTransferResponse.getExceptionsFileName(), matchesPattern(GenericFileTransferService.EXCEPTIONS_FILE_PREFIX+".+"+GenericFileTransferService.EXCEPTIONS_FILE_SUFFIX)),
				() -> assertThat("elapsedTime expected null", fileTransferResponse.getProcessingStats().getElapsedTime(), notNullValue()));
		
		
		var exceptionLines = readExceptionsFileLines(fileTransferResponse.getExceptionsFileName());
		validateExceptionsFileHeaderLines(exceptionLines, csvResourceName, numberOfLinesInFile, numberOfExceptions);
		
		assertThat("exceptionLines", exceptionLines, hasSize(6)); // 5 for the headers
		
		var exceptionLine = exceptionLines.get(5);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine[0], is("4")),
				() -> assertThat("Error Message", exceptionLine[1], is("Country is not Canada or United States")),

				() -> assertThat("sourceCsvLineNumber", exceptionLine[2], is("4")),
				() -> assertThat("Transaction_date", exceptionLine[3], is("01/04/2009 12:56")),
				() -> assertThat("Product", exceptionLine[4], is("Product2")),
				() -> assertThat("Price", exceptionLine[5], is("3600")),
				() -> assertThat("Payment_Type", exceptionLine[6], is("Visa")),
				() -> assertThat("Name", exceptionLine[7], is("Gerd W")),
				() -> assertThat("City", exceptionLine[8], is("Cahaba Heights")),
				() -> assertThat("State", exceptionLine[9], is("AL")),
				() -> assertThat("Country", exceptionLine[10], is("Germany")),
				() -> assertThat("Account_Created", exceptionLine[11], is("11/15/08 15:47")),
				() -> assertThat("Last_Login", exceptionLine[12], is("1/4/2009 12:45")),
				() -> assertThat("Latitude", exceptionLine[13], is("33.52056")),
				() -> assertThat("Longitude", exceptionLine[14], is("-86.8025")),
				() -> assertThat("US Zip", exceptionLine[15], is("35243")),
				
				() -> assertThat("Processing Stage", exceptionLine[17], is("SalesFileTransformerStage1")));
	}

	@Test
	void testParseAndSave_withInvalidPaymentType() throws IOException, URISyntaxException {
		var csvResourceName = "GenericFileTransferService/sales-bad-file-invalid-payment-type.csv";
		var numberOfLinesInFile = 8l;
		var numberOfExceptions = 1;
		var inputStream = getClass().getClassLoader().getResourceAsStream(csvResourceName);
		
		var fixture = prepareFixture(csvResourceName, List.of(new SalesFileTransformerStage1()), List.of(new SalesBeanTransformerStage1()));
		
		var fileTransferResponse = fixture.parseAndSave(UploadTableEnum.SALES, csvResourceName, inputStream, true);
		LOGGER.info("fileTransferResponse: {}", fileTransferResponse);
		
		assertAll("fileTransferResponse",
				() -> assertThat("fileTransferResponse", fileTransferResponse, notNullValue()),
				() -> assertThat("numberOfLinesInFile",fileTransferResponse.getProcessingStats().getNumberOfLinesInFile(), is(numberOfLinesInFile)),
				() -> assertThat("numberOfExceptions", fileTransferResponse.getProcessingStats().getNumberOfExceptions(), is(numberOfExceptions)),
				() -> assertThat("exceptionsFileName", fileTransferResponse.getExceptionsFileName(), matchesPattern(GenericFileTransferService.EXCEPTIONS_FILE_PREFIX+".+"+GenericFileTransferService.EXCEPTIONS_FILE_SUFFIX)),
				() -> assertThat("elapsedTime expected null", fileTransferResponse.getProcessingStats().getElapsedTime(), notNullValue()));
		
		
		var exceptionLines = readExceptionsFileLines(fileTransferResponse.getExceptionsFileName());
		validateExceptionsFileHeaderLines(exceptionLines, csvResourceName, numberOfLinesInFile, numberOfExceptions);
		
		assertThat("exceptionLines", exceptionLines, hasSize(6)); // 5 for the headers
		
		var exceptionLine = exceptionLines.get(5);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine[0], is("4")),
				() -> assertThat("Error Message", exceptionLine[1], is("Payment type [American Express] not valid.")),

				() -> assertThat("sourceCsvLineNumber", exceptionLine[2], is("4")),
				() -> assertThat("Transaction_date", exceptionLine[3], is("01/04/2009 12:56")),
				() -> assertThat("Product", exceptionLine[4], is("Product2")),
				() -> assertThat("Price", exceptionLine[5], is("3600")),
				() -> assertThat("Payment_Type", exceptionLine[6], is("American Express")),
				() -> assertThat("Name", exceptionLine[7], is("Gerd W")),
				() -> assertThat("City", exceptionLine[8], is("Scarborough")),
				() -> assertThat("State", exceptionLine[9], is("ON")),
				() -> assertThat("Country", exceptionLine[10], is("Canada")),
				() -> assertThat("Account_Created", exceptionLine[11], is("11/15/08 15:47")),
				() -> assertThat("Last_Login", exceptionLine[12], is("1/4/2009 12:45")),
				() -> assertThat("Latitude", exceptionLine[13], is("33.52056")),
				() -> assertThat("Longitude", exceptionLine[14], is("-86.8025")),
				() -> assertThat("US Zip", exceptionLine[15], is("35243")),
				
				() -> assertThat("Processing Stage", exceptionLine[17], is("SalesBeanTransformerStage1")));
	}

	
	/**
	 * Tests exceptions generated in 1) file transformer 2) CSV to Bean and 3) Bean transformer stage
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	void testParseAndSave_withExceptionInAllStagesOfProcessing() throws IOException, URISyntaxException {
		var csvResourceName = "GenericFileTransferService/sales-bad-file-exceptions-in-all-stages-of-processing.csv";
		var numberOfLinesInFile = 10l;
		var numberOfExceptions = 3;
		var inputStream = getClass().getClassLoader().getResourceAsStream(csvResourceName);
		
		var fixture = prepareFixture(csvResourceName, List.of(new SalesFileTransformerStage1()), List.of(new SalesBeanTransformerStage1()));
		
		var fileTransferResponse = fixture.parseAndSave(UploadTableEnum.SALES, csvResourceName, inputStream, true);
		LOGGER.info("fileTransferResponse: {}", fileTransferResponse);
		
		assertAll("fileTransferResponse",
				() -> assertThat("fileTransferResponse", fileTransferResponse, notNullValue()),
				() -> assertThat("numberOfLinesInFile",fileTransferResponse.getProcessingStats().getNumberOfLinesInFile(), is(numberOfLinesInFile)),
				() -> assertThat("numberOfExceptions", fileTransferResponse.getProcessingStats().getNumberOfExceptions(), is(numberOfExceptions)),
				() -> assertThat("exceptionsFileName", fileTransferResponse.getExceptionsFileName(), matchesPattern(GenericFileTransferService.EXCEPTIONS_FILE_PREFIX+".+"+GenericFileTransferService.EXCEPTIONS_FILE_SUFFIX)),
				() -> assertThat("elapsedTime expected null", fileTransferResponse.getProcessingStats().getElapsedTime(), notNullValue()));
		
		
		var exceptionLines = readExceptionsFileLines(fileTransferResponse.getExceptionsFileName());
		validateExceptionsFileHeaderLines(exceptionLines, csvResourceName, numberOfLinesInFile, numberOfExceptions);
		
		assertThat("exceptionLines", exceptionLines, hasSize(8)); // 5 for the headers
		
		// exception line from SalesBeanTransformerStage1
		var exceptionLine1 = exceptionLines.get(5);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine1));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine1[0], is("4")),
				() -> assertThat("Error Message", exceptionLine1[1], is("Payment type [American Express] not valid.")),

				() -> assertThat("sourceCsvLineNumber", exceptionLine1[2], is("4")),
				() -> assertThat("Transaction_date", exceptionLine1[3], is("01/04/2009 12:56")),
				() -> assertThat("Product", exceptionLine1[4], is("ProductBadPaymentType")),
				() -> assertThat("Price", exceptionLine1[5], is("3600")),
				() -> assertThat("Payment_Type", exceptionLine1[6], is("American Express")),
				() -> assertThat("Name", exceptionLine1[7], is("Gerd W")),
				() -> assertThat("City", exceptionLine1[8], is("Scarborough")),
				() -> assertThat("State", exceptionLine1[9], is("ON")),
				() -> assertThat("Country", exceptionLine1[10], is("Canada")),
				() -> assertThat("Account_Created", exceptionLine1[11], is("11/15/08 15:47")),
				() -> assertThat("Last_Login", exceptionLine1[12], is("1/4/2009 12:45")),
				() -> assertThat("Latitude", exceptionLine1[13], is("33.52056")),
				() -> assertThat("Longitude", exceptionLine1[14], is("-86.8025")),
				() -> assertThat("US Zip", exceptionLine1[15], is("35243")),
				
				() -> assertThat("Processing Stage", exceptionLine1[17], is("SalesBeanTransformerStage1")));
		
		// exception line from SalesFileTransformerStage1
		var exceptionLine2 = exceptionLines.get(6);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine2));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine2[0], is("7")),
				() -> assertThat("Error Message", exceptionLine2[1], is("Country is not Canada or United States")),

				() -> assertThat("sourceCsvLineNumber", exceptionLine2[2], is("7")),
				() -> assertThat("Transaction_date", exceptionLine2[3], is("01/04/1999 13:16")),
				() -> assertThat("Product", exceptionLine2[4], is("ProductBadCountry")),
				() -> assertThat("Price", exceptionLine2[5], is("3612")),
				() -> assertThat("Payment_Type", exceptionLine2[6], is("Visa")),
				() -> assertThat("Name", exceptionLine2[7], is("Gerd W")),
				() -> assertThat("City", exceptionLine2[8], is("Cahaba Heights")),
				() -> assertThat("State", exceptionLine2[9], is("AL")),
				() -> assertThat("Country", exceptionLine2[10], is("Germany")),
				() -> assertThat("Account_Created", exceptionLine2[11], is("11/15/08 15:47")),
				() -> assertThat("Last_Login", exceptionLine2[12], is("1/4/2009 12:45")),
				() -> assertThat("Latitude", exceptionLine2[13], is("33.52056")),
				() -> assertThat("Longitude", exceptionLine2[14], is("-86.8025")),
				() -> assertThat("US Zip", exceptionLine2[15], is("35243")),
				
				() -> assertThat("Processing Stage", exceptionLine2[17], is("SalesFileTransformerStage1")));

		// exception line from CSV file to beans(opencsv)
		var exceptionLine = exceptionLines.get(7);
		LOGGER.info("exceptionLine: {}", String.join(",", exceptionLine));
		assertAll("exceptionsLine",
				() -> assertThat("Line #", exceptionLine[0], is("9")),
				() -> assertThat("Error Message", exceptionLine[1], is("Conversion of XXXX to java.lang.Double failed.")),
				
				() -> assertThat("sourceCsvLineNumber", exceptionLine[2], is("10")),
				() -> assertThat("Transaction_date", exceptionLine[3], is("01/02/2022 14:43")),
				() -> assertThat("Product", exceptionLine[4], is("ProductBadNumericField")),
				() -> assertThat("Price", exceptionLine[5], is("XXXX")),
				() -> assertThat("Payment_Type", exceptionLine[6], is("Visa")),
				() -> assertThat("Name", exceptionLine[7], is("Gloria")),
				() -> assertThat("City", exceptionLine[8], is("Dayton")),
				() -> assertThat("State", exceptionLine[9], is("OH")),
				() -> assertThat("Country", exceptionLine[10], is("USA")),
				() -> assertThat("Account_Created", exceptionLine[11], is("1/2/2019 04:42")),
				() -> assertThat("Last_Login", exceptionLine[12], is("1/2/2001 07:49")),
				() -> assertThat("Latitude", exceptionLine[13], is("39.195")),
				() -> assertThat("Longitude", exceptionLine[14], is("-93.12345")),
				() -> assertThat("US Zip", exceptionLine[15], is("54321")),
				
				() -> assertThat("Processing Stage", exceptionLine[17], is("CSV file to beans(opencsv)")));

	}

	private List<String[]> readExceptionsFileLines(String exceptionsFileName) {
		Path exceptionsFilePath = Path.of(TEMP_DIR, exceptionsFileName);
		List<String[]> exceptionLines = new ArrayList<>();

		try (var csvReader = new CSVReader(new BufferedReader(new FileReader(exceptionsFilePath.toFile())))) {
			String[] cells;
			try {
				while ((cells = csvReader.readNext()) != null) {
					
						exceptionLines.add(cells);
				}
			} catch (CsvValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			assertThat(String.format("Exceptions file %s not found", exceptionsFilePath), true, is(false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exceptionLines;
	}
	private void validateExceptionsFileHeaderLines(List<String[]> exceptionLines, String fileName, long numberOfDataRows, int numberOfErrors) {

		assertAll("exceptionsFileHeaderLines",
			() -> assertThat("fileName", exceptionLines.get(0)[1], is(fileName)),
			() -> assertThat("numberOfDataRows", exceptionLines.get(1)[1], is(String.valueOf(numberOfDataRows))),
			() -> assertThat("numberOfErrors", exceptionLines.get(2)[1], is(String.valueOf(numberOfErrors))));
	}
	
	private GenericFileTransferService prepareFixture(String csvResourceName, List<ICsvFileTransformer> csvFileTransformerList, List<IBeanTransformer> beanTransformerList) {
		var csvFileTransformerService = new CsvFileTransformerService(csvFileTransformerList);
		var beanTransformerService = new BeanTransformerService(beanTransformerList);
		GenericFileTransferService genericFileTransferService = new GenericFileTransferService(entityRepositoryFactory, csvFileTransformerService, beanTransformerService, beanReferentialEntityEnrichmentService);
		// Use doReturn() which type unsafe instead of when() becuase when as below line does not compile
		//when(entityRepositoryFactory.getTableRepository(any())).thenReturn((BaseTableRepository<AbstractPersistableEntity, Serializable>)tableRepository);
		doReturn(tableRepository).when(entityRepositoryFactory).getTableRepository(any());
		//when(entityRepositoryFactory.getRepository(UploadTableEnum.SALES)).thenReturn(entityRepository);
		doReturn(entityRepository).when(entityRepositoryFactory).getRepository(any());
		when(entityRepository.count()).thenReturn(0l);
		return genericFileTransferService;
	}
}
