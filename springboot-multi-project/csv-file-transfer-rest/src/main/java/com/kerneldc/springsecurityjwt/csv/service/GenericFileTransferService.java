package com.kerneldc.springsecurityjwt.csv.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.springsecurityjwt.csv.controller.FileTransferResponse;
import com.kerneldc.springsecurityjwt.csv.repository.EntityRepositoryFactory;
import com.kerneldc.springsecurityjwt.csv.service.ProcessingStats.ProcessingStatsBuilder;
import com.kerneldc.springsecurityjwt.csv.service.transformer.BeanTransformerService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenericFileTransferService /*implements IFileTransferService*/ {
	private static final DateTimeFormatter EXCEPTIONS_FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
	public static final String EXCEPTIONS_FILE_PREFIX = "fileTransferController-csv-exceptions-"; 
	public static final String EXCEPTIONS_FILE_SUFFIX = ".csv";
	
	private final EntityRepositoryFactory entityRepositoryFactory;
	private final BeanTransformerService beanTransformerService;

	@SuppressWarnings("preview")
	protected record CsvParseResults (List<String[]> inputCsvLineList, String[] columnNames, List<? extends AbstractPersistableEntity> beanList, boolean majorException, List<CsvException> csvParseExceptionList) {
		public CsvParseResults withBeanList(List<? extends AbstractPersistableEntity> beanList) {
			return new CsvParseResults(inputCsvLineList(), columnNames(), beanList, majorException(), csvParseExceptionList());
		}
	} 
	@SuppressWarnings("preview")
    private record ExceptionsFileLine (long lineNumber, String exceptionMessage, String data) {public long getLineNumber() {return lineNumber;}}

	private StopWatch stopWatch = StopWatch.create();

	//@Transactional
	public FileTransferResponse parseAndSave(IEntityEnum uploadTableEnum, String clientFilename, BufferedReader bufferedReader, Boolean truncateTable) throws IOException {
		stopWatch.reset();
		stopWatch.start();

		var csvParseResults = parseFile(uploadTableEnum, bufferedReader);
		var processingStats = gatherStats(csvParseResults);
		
		var parseExceptionsFileLineList = transformToExceptionsFileLineList(csvParseResults.csvParseExceptionList);
		
		var fileTransferResponseBuilder = FileTransferResponse.builder().processingStats(processingStats);

		var beanTransformerResult = beanTransformerService.applyTransformers(uploadTableEnum, csvParseResults.beanList);
		if (CollectionUtils.isNotEmpty(beanTransformerResult.transformerExceptionList())) {
			// temp
			beanTransformerResult.transformerExceptionList().forEach(transformerException -> {
				LOGGER.error(transformerException.getMessage());
			});
			// TODO refactor. this code should be in a service that 1) transforms csv 2) parse csv to beans 3) transform beans 4) persist beans
//			var exception = beanTransformerResult.transformerException();
//			var exceptionMessage = exception.getMessage() + (exception.getCause() != null ? exception.getCause().getMessage() : StringUtils.EMPTY);  
//			return ResponseEntity.ok(new FileTransferResponse(exceptionMessage, null, null));
		}

		// temp, consider creating a withBeanList method in CsvParseResults record
		csvParseResults = csvParseResults.withBeanList(beanTransformerResult.beanList());
		
		
		var persistExceptionsFileLineList = persistBeans(uploadTableEnum, truncateTable, csvParseResults);
		var exceptionsFileLineList = new ArrayList<>(parseExceptionsFileLineList);
		if (CollectionUtils.isNotEmpty(persistExceptionsFileLineList)) {
			exceptionsFileLineList.addAll(persistExceptionsFileLineList);
			processingStats.incrementExceptionsCounts(persistExceptionsFileLineList.size());
		}

		// test begin
		var repository = entityRepositoryFactory.getRepository(uploadTableEnum);
		long count = repository.count();
		LOGGER.debug("Records in table: {}", count);
		// test end
		
		if (CollectionUtils.isNotEmpty(exceptionsFileLineList)) {
			var exceptionsFile = createExceptionsFile();
			// sort exceptionRecordList by line number
			exceptionsFileLineList.sort(Comparator.comparingLong(ExceptionsFileLine::getLineNumber));
			populateExceptionsFile(exceptionsFile, clientFilename, csvParseResults.columnNames, exceptionsFileLineList, processingStats);
			LOGGER.info("Written exceptions to file: {}", exceptionsFile);
			fileTransferResponseBuilder.exceptionsFileName(exceptionsFile.getFileName().toString());
		}
	
		stopWatch.stop();
		processingStats.setElapsedTime(stopWatch.toString());
		
		return fileTransferResponseBuilder.build();
	}	

	private CsvParseResults parseFile(IEntityEnum uploadTableEnum, BufferedReader bufferedReader) {
		CsvParseResults csvParseResults;
		
		@SuppressWarnings("unchecked")
		var entityType = (Class<? extends AbstractPersistableEntity>)uploadTableEnum.getEntity();
		var mappingStrategy = getMappingStrategy(entityType);
		CsvToBean<AbstractPersistableEntity> csvToBean = new CsvToBeanBuilder<AbstractPersistableEntity>(bufferedReader).withMappingStrategy(mappingStrategy)
			.withType(entityType).withThrowExceptions(false).withIgnoreEmptyLine(true).build();

		List<AbstractPersistableEntity> beans;
		try {
			beans = csvToBean.parse();
			csvParseResults = new CsvParseResults(mappingStrategy.getInputCsvLineList(), mappingStrategy.getColumnNames(), beans, false, csvToBean.getCapturedExceptions());
			LOGGER.debug("split: {}", String.join(",", mappingStrategy.getColumnNames()));
		} catch (RuntimeException e) {
			csvParseResults = new CsvParseResults(List.of(), new String[0], List.of(), true, List.of(captureMajorException(e)));
		}
		
		return csvParseResults;
	}

	private List<ExceptionsFileLine> persistBeans(IEntityEnum uploadTableEnum, Boolean truncateTable, CsvParseResults csvParseResults) {
		List<ExceptionsFileLine> persistExceptionsFileLineList = new ArrayList<>();
		var repository = entityRepositoryFactory.getTableRepository(uploadTableEnum);
		if (BooleanUtils.isTrue(truncateTable)) {
			repository.deleteAll();
		} else {
			repository.deleteListByLogicalKeyHolder(csvParseResults.beanList);
		}

		try {
			repository.saveAllTransaction(csvParseResults.beanList);
		} catch (Exception e) {
			LOGGER.error("Batch persist failed with: {}", StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : e.getCause().getMessage());
			LOGGER.error("Trying single row persist.");
			var lineNumber = 0;
			for (AbstractPersistableEntity bean : csvParseResults.beanList) {
				lineNumber++;
				try {
					repository.saveTransaction(bean);
				} catch (Exception e1) {
					LOGGER.info("Exception while saving row #{}: {}", lineNumber, bean);
					var exceptionMessage = e1.getMessage() != null ? e1.getMessage() : e1.getCause().getMessage();
					var csvLine = String.join(",", csvParseResults.inputCsvLineList.get(lineNumber-1));
					persistExceptionsFileLineList.add(new ExceptionsFileLine(lineNumber, exceptionMessage, csvLine));
				}
			}
		}
		return persistExceptionsFileLineList;
	}

	private <T> ValidatingHeaderColumnNameMappingStrategy<T> getMappingStrategy(Class<? extends T> type) {
		ValidatingHeaderColumnNameMappingStrategy<T> mappingStrategy = new ValidatingHeaderColumnNameMappingStrategy<>();
		mappingStrategy.setType(type);
		return mappingStrategy;
	}
	
	private List<ExceptionsFileLine> transformToExceptionsFileLineList(List<CsvException> csvParseExceptionList) {
		return csvParseExceptionList.stream().map(exception -> {
			var exceptionMessage = exception.getMessage() != null ? exception.getMessage() : exception.getCause().getMessage();
			var data = String.join(", ", exception.getLine());
			
			return new ExceptionsFileLine(exception.getLineNumber(), exceptionMessage, data);
		}).collect(Collectors.toList());
	}

	private ProcessingStats gatherStats(CsvParseResults csvParseResults) {
		ProcessingStatsBuilder builder = ProcessingStats.builder();
		if (csvParseResults.majorException) {
			builder.numberOfExceptions(csvParseResults.csvParseExceptionList.size());
		} else {
			builder
				.numberOfLinesInFile(csvParseResults.inputCsvLineList.size())
				.numberOfExceptions(csvParseResults.csvParseExceptionList.size());
		}
		return builder.build();
	}
    private Path createExceptionsFile() throws IOException {
		long threadId = Thread.currentThread().getId();
		return Files.createTempFile(EXCEPTIONS_FILE_PREFIX+getNowString()+"-"+threadId, EXCEPTIONS_FILE_SUFFIX);
	}

    private void populateExceptionsFile(Path exceptionsFile, String clientFilename, String[] columnNames, List<ExceptionsFileLine> exceptionRecordList, ProcessingStats processingStats) throws IOException {
		List<String> exceptionLines = new ArrayList<>();

		exceptionLines.add("File name:,"+clientFilename);
		if (processingStats.getNumberOfLinesInFile() != null) {
			exceptionLines.add(csvLineFromFields("Number of rows:", processingStats.getNumberOfLinesInFile())); 
		}
		if (processingStats.getNumberOfExceptions() != null) {
			exceptionLines.add(csvLineFromFields("Number of errors:", processingStats.getNumberOfExceptions())); 
		}
		exceptionLines.add(StringUtils.EMPTY);
		
		exceptionLines.add("Line #,Error Message,"+String.join(",", columnNames));
		exceptionRecordList.stream().forEach(
				exceptionRecord -> exceptionLines.add(String.join(",", Long.toString(exceptionRecord.lineNumber),
						StringEscapeUtils.escapeCsv(exceptionRecord.exceptionMessage), exceptionRecord.data)));
		FileUtils.writeLines(exceptionsFile.toFile(), exceptionLines);		
	}

    private String csvLineFromFields(Object...objects) {
    	var csvLine = new StringBuilder();
    	for (Object object: objects) {
    		csvLine.append(StringEscapeUtils.escapeCsv(object.toString())).append(",");
    	}
    	return csvLine.length() > 1 ? csvLine.substring(0, csvLine.length() - 1) : StringUtils.EMPTY;
    }
    
	public static String getNowString() {
		var now = LocalDateTime.now();
		return now.format(EXCEPTIONS_FILE_TIMESTAMP_FORMAT);
	}
	
	protected CsvException captureMajorException(RuntimeException e) {
		String messageWithCause = e.getMessage() + (e.getCause() != null ? ", "	+ e.getCause().getMessage() : StringUtils.EMPTY);
		var csvException = new CsvException(messageWithCause);
		csvException.setLineNumber(0);
		csvException.setLine(ArrayUtils.EMPTY_STRING_ARRAY);
		return csvException;
	}
	
	/**
	 * Read from entity and write to byte array
	 * @param uploadTableEnum
	 * @return
	 * @throws CsvDataTypeMismatchException
	 * @throws CsvRequiredFieldEmptyException
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public byte[] readAndWrite(IEntityEnum uploadTableEnum) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		var repository = entityRepositoryFactory.getRepository(uploadTableEnum);
		LOGGER.debug("uploadTableEnum: {}", uploadTableEnum);
		@SuppressWarnings("unchecked")
		List<AbstractEntity> beans = (List<AbstractEntity>)repository.findAll();
		LOGGER.debug("beans size: {}", beans.size());
		var byteArrayOutputstream = new ByteArrayOutputStream();
	    var outputStreamWriter = new OutputStreamWriter(byteArrayOutputstream);
		var beanToCsvBuilder = new StatefulBeanToCsvBuilder<AbstractEntity>(outputStreamWriter);
		
		if (ArrayUtils.isNotEmpty(uploadTableEnum.getWriteColumnOrder())) {
		    HeaderColumnNameMappingStrategy<AbstractEntity> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
		    mappingStrategy.setType(uploadTableEnum.getEntity());
		    mappingStrategy.setColumnOrderOnWrite(new FixedOrderComparator<>(uploadTableEnum.getWriteColumnOrder()));
		    beanToCsvBuilder.withMappingStrategy(mappingStrategy);
		}
		
		var beanToCsv = beanToCsvBuilder.build(); 
		beanToCsv.write(beans);
		outputStreamWriter.flush();
		outputStreamWriter.close();
		return byteArrayOutputstream.toByteArray();
	}
}
