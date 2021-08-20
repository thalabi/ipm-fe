package com.kerneldc.springsecurityjwt.csv.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.portfolio.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.springsecurityjwt.csv.domain.UploadTableEnum;
import com.kerneldc.springsecurityjwt.csv.service.GenericFileTransferService;
import com.kerneldc.springsecurityjwt.csv.service.transformer.CsvFileTransformerService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("fileTransferController")
@RequiredArgsConstructor
@Slf4j
public class FileTransferController {
	
	private final GenericFileTransferService genericFileTransferService;
	private final CsvFileTransformerService csvFileTransformerService;
	
    @GetMapping("/getTableList")
	public ResponseEntity<TableListResponse> getTableList() {
    	LOGGER.info("Begin ...");
    	var tableList = Arrays.asList(UploadTableEnum.values()).stream().map(Enum::toString).collect(Collectors.toList());
    	tableList.addAll(Arrays.asList(InvestmentPortfolioTableEnum.values()).stream().map(Enum::toString).collect(Collectors.toList()));
    	
		LOGGER.info("End ...");
    	return ResponseEntity.ok(TableListResponse.builder().tableList(tableList).build());
    }

	@PostMapping("/uploadFile")
	public ResponseEntity<FileTransferResponse> uploadFile(MultipartFile csvFile, String tableName, Boolean truncateTable) throws IOException {
    	LOGGER.info("Begin ...");
    	LOGGER.info("Received {}, size {} bytes", csvFile.getOriginalFilename(), csvFile.getSize());
    	LOGGER.info("tableName: {}", tableName);
    	LOGGER.info("truncateTable: {}", truncateTable);

    	var tableEnum = tableEnumFromString(tableName);
    	try (var bufferedReader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()));) {
    		
    		var csvFileTransformerResult = csvFileTransformerService.applyTransformers(tableEnum, bufferedReader);
    		if (csvFileTransformerResult.transformerException() != null) {
    			var exception = csvFileTransformerResult.transformerException();
    			var exceptionMessage = exception.getMessage() + (exception.getCause() != null ? exception.getCause().getMessage() : StringUtils.EMPTY);  
    			return ResponseEntity.ok(new FileTransferResponse(exceptionMessage, null, null));
    		}
    		
    		var fileTransferResponse = genericFileTransferService.parseAndSave(tableEnum, csvFile.getOriginalFilename(), csvFileTransformerResult.csvBufferedReader(), truncateTable);
    		LOGGER.info("End ...");
        	return ResponseEntity.ok(fileTransferResponse);
    	}
    }

    @GetMapping("/downloadExceptionsFile")
	public ResponseEntity<byte[]> downloadExceptionsFile(@RequestParam(value = "exceptionsFile") @Valid String exceptionsFile) throws IOException {
    	LOGGER.info("Begin ...");
    	var exceptionsFileFullPath = Path.of(FileUtils.getTempDirectoryPath() ,exceptionsFile);
    	LOGGER.info("Downloading file: {}", exceptionsFileFullPath);
    	var fileBytes = FileUtils.readFileToByteArray(exceptionsFileFullPath.toFile());
		LOGGER.info("End ...");
    	return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "exceptionsFile-"+GenericFileTransferService.getNowString()+".csv")
    	        .contentType(MediaType.parseMediaType("application/csv")).body(fileBytes);
    }

	@GetMapping("/downloadFile")
	public ResponseEntity<byte[]> downloadFile(String tableName) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
    	LOGGER.info("Begin ...");
    	var fileBytes = genericFileTransferService.readAndWrite(tableEnumFromString(tableName));
		
		LOGGER.info("End ...");
    	return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tableName.toLowerCase()+".csv")
    	        .contentType(MediaType.parseMediaType("application/csv")).body(fileBytes);
    }
	
	private IEntityEnum tableEnumFromString(String tableName) {
    	var upperTableName = tableName.toUpperCase();
		return ObjectUtils.defaultIfNull(UploadTableEnum.valueIfPresent(upperTableName),
				InvestmentPortfolioTableEnum.valueIfPresent(upperTableName));
	}
}
