package com.kerneldc.ipm.rest.csv.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvChainedException;
import com.opencsv.exceptions.CsvFieldAssignmentException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * A CSV mapping strategy that:
 * 1. validates that the file headers match (case insensitive and ignoring white space) to column names defined in bean
 * 2. captures column names
 * 3. captures input csv lines in synchronized inputCsvLineList
 * 4. populates bean with the csv line
 */
@Slf4j
public class ValidatingHeaderColumnNameMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

	private String[] columnNames;

	private final List<String[]> inputCsvLineList = Collections.synchronizedList(new ArrayList<>());
	
	@Override
	public void captureHeader(CSVReader reader) throws IOException, CsvRequiredFieldEmptyException {
		super.captureHeader(reader);
		validateHeader();
		
		columnNames = headerIndex.getHeaderIndex();
	}
	
	@Override
	public T populateNewBean(String[] line)	throws CsvBeanIntrospectionException, CsvFieldAssignmentException, CsvChainedException {
		inputCsvLineList.add(line);
		//LOGGER.debug("inputCsvLineList.size(): {}", inputCsvLineList.size());
		var bean = super.populateNewBean(line);
		((AbstractPersistableEntity)bean).setSourceCsvLine(line);
		return bean;
		//return super.populateNewBean(line);
	}
		
	/**
	 * Validates that the the file headers match the ones defined in the bean 
	 */
	private void validateHeader() {
		List<String> headers = Arrays.asList(headerIndex.getHeaderIndex());
		LOGGER.debug("Headers: {}", String.join(", ", headers));
		
		LOGGER.debug("Number of headers: {}", headerIndex.getHeaderIndexLength());
		LOGGER.debug("Number of bean fields: {}", super.getFieldMap().values().size());
		List<String> expectedHeaderNameList = super.getFieldMap().values().stream().map(beanField -> {
			String fieldName = beanField.getField().getName();
			String columnName = beanField.getField().getDeclaredAnnotation(CsvBindByName.class).column();
			return StringUtils.isNotEmpty(columnName) ? columnName : fieldName;
		}).toList();
		LOGGER.debug("Expected headers: {}", String.join(", ", expectedHeaderNameList));

		// convert lists to lower case and remove white space
		UnaryOperator<String> toLowerAndWithoutWhiteSpace = s -> s.toLowerCase().replaceAll("\\s", StringUtils.EMPTY);
		List<String> headersLowerCase = headers.stream().map(toLowerAndWithoutWhiteSpace).collect(Collectors.toList());
		List<String> expectedHeadersLowerCase = expectedHeaderNameList.stream().map(toLowerAndWithoutWhiteSpace).collect(Collectors.toList());
		// sort both lists before checking if they are equal
		Collections.sort(headersLowerCase);
		Collections.sort(expectedHeadersLowerCase);
		LOGGER.debug("headersLowerCase: {}", String.join(", ", headersLowerCase));
		LOGGER.debug("expectedHeadersLowerCase: {}", String.join(", ", expectedHeadersLowerCase));
		if (! /* not */ headersLowerCase.equals(expectedHeadersLowerCase)){
			throw new IllegalStateException(String.format("File header [%s] does not match expected header [%s]", String.join(", ", headersLowerCase), String.join(", ", expectedHeadersLowerCase)));
		}
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public List<String[]> getInputCsvLineList() {
		return inputCsvLineList;
	}
}
