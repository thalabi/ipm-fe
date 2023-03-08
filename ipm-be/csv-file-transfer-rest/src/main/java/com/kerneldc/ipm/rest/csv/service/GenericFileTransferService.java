package com.kerneldc.ipm.rest.csv.service;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.controller.FileTransferResponse;
import com.kerneldc.ipm.rest.csv.repository.EntityRepositoryFactory;
import com.kerneldc.ipm.rest.csv.service.ProcessingStats.ProcessingStatsBuilder;
import com.kerneldc.ipm.rest.csv.service.enrichment.BeanReferentialEntityEnrichmentService;
import com.kerneldc.ipm.rest.csv.service.transformer.BeanTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.CsvFileTransformerService;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.BeanTransformerException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.CsvTransformerException;
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
	private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss.SSS");
	public static final String TEMP_FILES_PREFIX = "ipm-application";
	public static final String EXCEPTIONS_FILE_PREFIX = TEMP_FILES_PREFIX + "-" + "file-transfer-service-csv-exceptions-"; 
	public static final String EXCEPTIONS_FILE_SUFFIX = ".csv";
	

	private final EntityRepositoryFactory entityRepositoryFactory;
	private final CsvFileTransformerService csvFileTransformerService;
	private final BeanTransformerService beanTransformerService;
	private final BeanReferentialEntityEnrichmentService beanReferentialEntityEnrichmentService;

//	protected record CsvToBeanParseResults(/* List<String[]> inputCsvLineList, */String[] columnNames,
//			List<? extends AbstractPersistableEntity> beanList,
//			List<CsvException> csvToBeanParseExceptionList) {
//		public CsvToBeanParseResults() {
//			this(/* List.of(), */new String[0], List.of(), List.of());
//		}
//	}

	// represents a detail line in the exceptions file
	public record ExceptionsFileLine (long lineNumber, String exceptionMessage, String csvData, String beanData, String transformerName) {
    	public ExceptionsFileLine (long lineNumber, String exceptionMessage, String csvData, String beanData) {
    		this(lineNumber, exceptionMessage, csvData, beanData, StringUtils.EMPTY);
    	}
    	public ExceptionsFileLine (long lineNumber, String exceptionMessage, String csvData) {
    		this(lineNumber, exceptionMessage, csvData, StringUtils.EMPTY, StringUtils.EMPTY);
    	}
    	public long getLineNumber() {
    		return lineNumber;
    	}
    }

	private StopWatch stopWatch = StopWatch.create();

	public FileTransferResponse parseAndSave(IEntityEnum uploadTableEnum, String clientFilename, InputStream inputStream, Boolean truncateTable) throws IOException {
		stopWatch.reset();
		stopWatch.start();

		
		//List<ExceptionsFileLine> persistExceptionsFileLineList = List.of();
		ProcessingStats processingStats;

		var context = new FileProcessingContext(uploadTableEnum);
		try {
			// Four stages
			// Step 1. apply transformers on the cvs file
			LOGGER.info("Applying CSV transformers.");
			csvFileTransformerService.applyTransformers(inputStream, context);
			// Step 2. parse csv file and convert to beans
			LOGGER.info("Parsing CSV file (to beans).");
			parseCsvFileToBeans(context);
			// Step 3. apply transformers on beans
			LOGGER.info("Applying bean transformers.");
			beanTransformerService.applyTransformers(context);
			// Step 4. persist beans
			LOGGER.info("Persisting beans.");
			persistBeans(truncateTable, context);
			getRecordCount(uploadTableEnum);
		} catch (AbortFileProcessingException e) {
			context.setAbortFileProcessingException(e);
		}
		processingStats = gatherStats(context);

		
		// collect exceptions if any and generate exceptions file
		var csvTransformersExceptionsFileLineList = transformCsvTransformerExceptionsToExceptionsFileLineList(context.getCsvTransformerExceptionList());
		var csvToBeanExceptionsFileLineList = transformCsvToBeanExceptionsToExceptionsFileLineList(context.getCsvToBeanParseExceptionList());
		var beanTransformersExceptionsFileLineList = transformBeanExceptionsToExceptionsFileLineList(context.getBeanTransformerExceptionList());
		
		var fileTransferResponseBuilder = FileTransferResponse.builder().processingStats(processingStats);


		var exceptionsFileLineList = new ArrayList<>(csvTransformersExceptionsFileLineList);
		exceptionsFileLineList.addAll(csvToBeanExceptionsFileLineList);
		exceptionsFileLineList.addAll(beanTransformersExceptionsFileLineList);
		exceptionsFileLineList.addAll(context.getPersistExceptionsFileLineList());
		
		if (context.getAbortFileProcessingException() != null) {
			exceptionsFileLineList.add(transformAbortFileProcessingExceptionToExceptionsFileLine(context.getAbortFileProcessingException()));
		}

		
		if (CollectionUtils.isNotEmpty(exceptionsFileLineList)) {
			var exceptionsFile = createExceptionsFile();
			// sort exceptionRecordList by line number
			exceptionsFileLineList.sort(Comparator.comparingLong(ExceptionsFileLine::getLineNumber));
			populateExceptionsFile(exceptionsFile, clientFilename, context.getEntityColumnNames(), exceptionsFileLineList, processingStats);
			LOGGER.info("Written exceptions to file: {}", exceptionsFile);
			fileTransferResponseBuilder.exceptionsFileName(exceptionsFile.getFileName().toString());
		}
	
		context.clear();
		stopWatch.stop();
		processingStats.setElapsedTime(stopWatch.toString());
		
		return fileTransferResponseBuilder.build();
	}	

	protected void getRecordCount(IEntityEnum uploadTableEnum) {
		// test begin
		var repository = entityRepositoryFactory.getRepository(uploadTableEnum);
		long count = repository.count();
		LOGGER.debug("Records in table: {}", count);
		// test end
	}

	protected void parseCsvFileToBeans(FileProcessingContext context) throws AbortFileProcessingException {
		
		//var context = FileProcessingContext.get();
		
		Path csvFile = context.getWorkInProgressFile();
		if (csvFile.toFile().length() == 0) {
			return; 
		}
		
		//CsvToBeanParseResults csvToBeanParseResults;
		var uploadTableEnum = context.getUploadTableEnum();
		
		@SuppressWarnings("unchecked")
		var entityType = (Class<? extends AbstractPersistableEntity>)uploadTableEnum.getEntity();
		var mappingStrategy = getMappingStrategy(entityType);
		CsvToBean<AbstractPersistableEntity> csvToBean = null;
		try {
			csvToBean = new CsvToBeanBuilder<AbstractPersistableEntity>(new FileReader(csvFile.toFile(), Charset.defaultCharset())).withMappingStrategy(mappingStrategy)
				/*.withType(entityType)*/.withThrowExceptions(false).withIgnoreEmptyLine(true).build();
		} catch (IllegalStateException | IOException e) {
			throw new AbortFileProcessingException(this.getClass().getSimpleName(),
					"Failed to create CsvToBeanBuilder.", e);
		}

		List<AbstractPersistableEntity> beans;
		try {
			beans = csvToBean.parse();
			//setSourceCsvLineNumber(beans, mappingStrategy.getInputCsvLineList());
//			csvToBeanParseResults = new CsvToBeanParseResults(
//					/* mappingStrategy.getInputCsvLineList(), */mappingStrategy.getColumnNames(), beans,
//					/* false, */ csvToBean.getCapturedExceptions());
			context.setEntityColumnNames(mappingStrategy.getColumnNames());
			context.getBeans().addAll(beans);
			context.getCsvToBeanParseExceptionList().addAll(csvToBean.getCapturedExceptions());
			
			LOGGER.debug("column names: {}", String.join(",", mappingStrategy.getColumnNames()));
		} catch (RuntimeException e) {
			throw new AbortFileProcessingException(this.getClass().getSimpleName(),
					"Failed to convert csv file to beans.", e);
		}
	}

//	private void setSourceCsvLineNumber(List<AbstractPersistableEntity> beans, List<String[]> inputCsvLineList) {
//		for (AbstractPersistableEntity bean: beans) {
//			var i = inputCsvLineList.indexOf(bean.getSourceCsvLine());
//			if (i == -1) {
//				throw new IllegalStateException(String.format("CSV line: [%s] was not found in inputCsvLineList", String.join(",", bean.getSourceCsvLine())));
//			}
//			bean.setSourceCsvLineNumber(i + 2l);
//		}
//	}

	protected void persistBeans(Boolean truncateTable, FileProcessingContext context) {
		
		//var context = FileProcessingContext.get();
		
		var uploadTableEnum = context.getUploadTableEnum();
		var beanList = context.getBeans();
		var persistExceptionsFileLineList = context.getPersistExceptionsFileLineList();

		var repository = entityRepositoryFactory.getTableRepository(uploadTableEnum);
		if (BooleanUtils.isTrue(truncateTable)) {
			repository.deleteAll();
		} else {
			repository.deleteListByLogicalKeyHolder(beanList);
		}

		try {
			repository.saveAllTransaction(beanList);
		} catch (Exception e) {
			LOGGER.error("Batch persist failed with: {}", StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : e.getCause().getMessage());
			LOGGER.error("Trying single row persist.");
			//var lineNumber = 0;
			for (AbstractPersistableEntity bean : beanList) {
				//lineNumber++;
				try {
					repository.saveTransaction(bean);
				} catch (Exception e1) {
					//LOGGER.info("Exception while saving row #{}: {}", lineNumber, bean);
					LOGGER.info("Exception while saving row #{}: {}", bean.getSourceCsvLineNumber(), bean);
					var exceptionMessage = ExceptionUtils.getRootCauseMessage(e1);
					var csvData = String.join(", ", bean.getSourceCsvLine());
					persistExceptionsFileLineList.add(new ExceptionsFileLine(bean.getSourceCsvLineNumber(), exceptionMessage, csvData, bean.toString(), "Beans persistance"));
				}
			}
		}
	}

	private <T> ValidatingHeaderColumnNameMappingStrategy<T> getMappingStrategy(Class<? extends T> type) {
		ValidatingHeaderColumnNameMappingStrategy<T> mappingStrategy = new ValidatingHeaderColumnNameMappingStrategy<>();
		mappingStrategy.setType(type);
		return mappingStrategy;
	}
	
	private List<ExceptionsFileLine> transformCsvTransformerExceptionsToExceptionsFileLineList(
			List<CsvTransformerException> csvTransformerExceptionList) {
		return csvTransformerExceptionList.stream().map(exception -> {
			var exceptionMessage = NestedExceptionUtils.getMostSpecificCause(exception).getMessage();
			var csvData = String.join(",", exception.getCells());

			return new ExceptionsFileLine(exception.getLineNumber(), exceptionMessage, csvData, StringUtils.EMPTY, exception.getTransformerName());
		}).toList();
	}
	private List<ExceptionsFileLine> transformCsvToBeanExceptionsToExceptionsFileLineList(List<CsvException> csvParseExceptionList) {
		return csvParseExceptionList.stream().map(exception -> {
			var exceptionMessage = (StringUtils.isEmpty(exception.getMessage())
					|| /* not */ exception.getCause() instanceof NumberFormatException
							? NestedExceptionUtils.getMostSpecificCause(exception).getMessage()
							: exception.getMessage());
			var csvData = String.join(",", exception.getLine());
			
			return new ExceptionsFileLine(exception.getLineNumber(), exceptionMessage, csvData, StringUtils.EMPTY, "CSV file to beans(opencsv)");
		}).toList();
	}
	
	private List<ExceptionsFileLine> transformBeanExceptionsToExceptionsFileLineList(List<BeanTransformerException> transformerExceptionList) {
		return transformerExceptionList.stream().map(exception -> {
			var exceptionMessage = NestedExceptionUtils.getMostSpecificCause(exception).getMessage();
			var csvData = String.join(",", exception.getBean().getSourceCsvLine());
			
			return new ExceptionsFileLine(exception.getBean().getSourceCsvLineNumber(), exceptionMessage, csvData, exception.getBean().toString(), exception.getTransformerName());
		}).toList();
	}

	private ExceptionsFileLine transformAbortFileProcessingExceptionToExceptionsFileLine (AbortFileProcessingException exception){
		var exceptionMessage = NestedExceptionUtils.getMostSpecificCause(exception).getMessage();
		return new ExceptionsFileLine(0, exceptionMessage, "", "", exception.getTransformerName());
	}
	
	private ProcessingStats gatherStats(FileProcessingContext context) {
		
		ProcessingStatsBuilder builder = ProcessingStats.builder();
		builder.numberOfLinesInFile(context.getSourceCsvDataRowsCount())
						.numberOfExceptions(context.getCsvTransformerExceptionList().size()
						+ context.getCsvToBeanParseExceptionList().size()
						+ context.getBeanTransformerExceptionList().size()
						+ context.getPersistExceptionsFileLineList().size()
						+ (context.getAbortFileProcessingException() == null ? 0 : 1));
		return builder.build();
	}
    private Path createExceptionsFile() throws IOException {
		long threadId = Thread.currentThread().getId();
		return Files.createTempFile(EXCEPTIONS_FILE_PREFIX+getNowString()+"-"+threadId, EXCEPTIONS_FILE_SUFFIX);
	}
	public static Path createTempFile() throws IOException {
		long threadId = Thread.currentThread().getId();
		return Files.createTempFile(GenericFileTransferService.TEMP_FILES_PREFIX+"-"+getNowString()+"-"+threadId, null);
	}

    private void populateExceptionsFile(Path exceptionsFile, String clientFilename, String[] columnNames, List<ExceptionsFileLine> exceptionRecordList, ProcessingStats processingStats) throws IOException {
		List<String> exceptionLines = new ArrayList<>();

		exceptionLines.add("File name:,"+clientFilename);
		if (processingStats.getNumberOfLinesInFile() != null) {
			exceptionLines.add(csvLineFromFields("Number of data rows:", processingStats.getNumberOfLinesInFile())); 
		}
		if (processingStats.getNumberOfExceptions() != null) {
			exceptionLines.add(csvLineFromFields("Number of errors:", processingStats.getNumberOfExceptions())); 
		}
		exceptionLines.add(StringUtils.EMPTY);
		
		exceptionLines.add("Line #,Error Message,"+String.join(",", columnNames)+",Bean,Processing Stage");
		exceptionRecordList.stream()
				.forEach(exceptionRecord -> exceptionLines.add(String.join(",",
						Long.toString(exceptionRecord.lineNumber),
						StringEscapeUtils.escapeCsv(exceptionRecord.exceptionMessage), exceptionRecord.csvData,
						StringEscapeUtils.escapeCsv(exceptionRecord.beanData), exceptionRecord.transformerName)));
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
		return now.format(FILE_TIMESTAMP_FORMAT);
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
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public byte[] readAndWrite(IEntityEnum uploadTableEnum) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException, IllegalArgumentException, NoSuchFieldException, SecurityException {
		var repository = entityRepositoryFactory.getRepository(uploadTableEnum);
		LOGGER.debug("uploadTableEnum: {}", uploadTableEnum);
		@SuppressWarnings("unchecked")
		List<AbstractEntity> beans = (List<AbstractEntity>)repository.findAll();

		// TODO handle exception list in beanReferentialEntityEnrichmentResult
		var beanReferentialEntityEnrichmentResult = beanReferentialEntityEnrichmentService.applyEnrichers(uploadTableEnum, beans);
		
		LOGGER.debug("beans size: {}", beans.size());
		var byteArrayOutputstream = new ByteArrayOutputStream();
	    var outputStreamWriter = new OutputStreamWriter(byteArrayOutputstream);
		var beanToCsvBuilder = new StatefulBeanToCsvBuilder<AbstractEntity>(outputStreamWriter);
		
		if (ArrayUtils.isNotEmpty(uploadTableEnum.getWriteColumnOrder())) {
			LOGGER.info("Table has writeColumnOrder defined. Defining a mappingStrategy");
			HeaderColumnNameMappingStrategy<AbstractEntity> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
		    mappingStrategy.setType(uploadTableEnum.getEntity());
		    
		    // so that sourceCsvLineNumber column is ignored. Not written to csv file.
		    MultiValuedMap<Class<?>, Field> ignoreFields = new ArrayListValuedHashMap<>(1, 1);
		    ignoreFields.put(AbstractPersistableEntity.class, AbstractPersistableEntity.class.getDeclaredField(CsvFileTransformerService.SOURCE_CSV_LINE_NUMBER));
		    mappingStrategy.ignoreFields(ignoreFields);
		    mappingStrategy.setProfile("csvWrite"); // this is still needed even though ignoreFields is set above
		    
		    mappingStrategy.setColumnOrderOnWrite(new FixedOrderComparator<>(uploadTableEnum.getWriteColumnOrder()));
		    beanToCsvBuilder.withMappingStrategy(mappingStrategy);
		} else {
			LOGGER.info("Table does not have a writeColumnOrder defined.");
			beanToCsvBuilder.withProfile("csvWrite"); // so that sourceCsvLineNumber column is ignored. Not written to csv file.
		}
		
		var beanToCsv = beanToCsvBuilder.build(); 
		beanToCsv.write(beans);
		outputStreamWriter.flush();
		outputStreamWriter.close();
		return byteArrayOutputstream.toByteArray();
	}
}
