package com.kerneldc.ipm.rest.csv.service.transformer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.service.GenericFileTransferService.ExceptionsFileLine;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.BeanTransformerException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.CsvTransformerException;
import com.opencsv.exceptions.CsvException;

import lombok.Getter;
import lombok.Setter;

public class FileProcessingContext {

	//public static final ThreadLocal<FileProcessingContext> threadLocalContext = new ThreadLocal<>();
	
	@Getter @Setter
	private IEntityEnum uploadTableEnum;
	@Getter @Setter
	private Path workInProgressFile;
	@Getter @Setter
	private Long sourceCsvDataRowsCount;
	@Getter @Setter
	private String[] sourceCsvHeaderColumns;
	@Getter @Setter
	private String[] entityColumnNames;
	@Getter  @Setter
	private List<AbstractPersistableEntity> beans;
	@Getter
	private List<CsvTransformerException> csvTransformerExceptionList;
	@Getter
	private List<CsvException> csvToBeanParseExceptionList;
	@Getter
	private List<BeanTransformerException> beanTransformerExceptionList;
	@Getter
	private List<ExceptionsFileLine> persistExceptionsFileLineList;
	@Getter @Setter
	private AbortFileProcessingException abortFileProcessingException;
	
	public FileProcessingContext(IEntityEnum uploadTableEnum) {
		this.uploadTableEnum = uploadTableEnum;
		workInProgressFile = Paths.get(StringUtils.EMPTY);
		sourceCsvDataRowsCount = 0l;
		sourceCsvHeaderColumns = new String[0];
		entityColumnNames = new String[0];
		beans = new ArrayList<>();
		csvTransformerExceptionList = new ArrayList<>();
		csvToBeanParseExceptionList = new ArrayList<>();
		beanTransformerExceptionList = new ArrayList<>();
		persistExceptionsFileLineList = new ArrayList<>();	
	}
	
//	public static FileProcessingContext init(IEntityEnum uploadTableEnum) {
//		var fileProcessingContext = new FileProcessingContext(uploadTableEnum);
//		//fileProcessingContext.uploadTableEnum = uploadTableEnum;
//		threadLocalContext.set(fileProcessingContext);
//		return fileProcessingContext;
//	}
	
//	public static FileProcessingContext get() {
//		return threadLocalContext.get();
//	}

//	public static void clear() {
//		var fileProcessingContext = threadLocalContext.get();
//		fileProcessingContext.uploadTableEnum = null;
//		fileProcessingContext.workInProgressFile = Paths.get(StringUtils.EMPTY);
//		fileProcessingContext.sourceCsvHeaderColumns = null;
//		fileProcessingContext.entityColumnNames = null;
//		fileProcessingContext.beans.clear();
//		fileProcessingContext.csvTransformerExceptionList.clear();
//		fileProcessingContext.csvToBeanParseExceptionList.clear();
//		fileProcessingContext.beanTransformerExceptionList.clear();
//		fileProcessingContext.persistExceptionsFileLineList.clear();
//	}
	public void clear() {
		uploadTableEnum = null;
		workInProgressFile = Paths.get(StringUtils.EMPTY);
		sourceCsvHeaderColumns = null;
		entityColumnNames = null;
		beans.clear();
		csvTransformerExceptionList.clear();
		csvToBeanParseExceptionList.clear();
		beanTransformerExceptionList.clear();
		persistExceptionsFileLineList.clear();
	}
}
