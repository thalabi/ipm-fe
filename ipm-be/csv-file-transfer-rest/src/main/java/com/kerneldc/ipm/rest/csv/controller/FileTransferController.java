package com.kerneldc.ipm.rest.csv.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
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
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.rest.csv.service.GenericFileTransferService;
import com.kerneldc.ipm.util.AppTimeUtils;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/fileTransferController")
@RequiredArgsConstructor
@Slf4j
public class FileTransferController {
	
	private final GenericFileTransferService genericFileTransferService;
	private static final String BEGIN = "Begin ...";
	private static final String END = "End ...";
	
    @GetMapping("/getTableList")
	public ResponseEntity<TableListResponse> getTableList() {
    	LOGGER.info(BEGIN);
    	var tableList = Arrays.asList(UploadTableEnum.values()).stream().map(Enum::toString).collect(Collectors.toList());
    	tableList.addAll(Arrays.asList(InvestmentPortfolioTableEnum.values()).stream().map(Enum::toString).toList());
    	
		LOGGER.info(END);
    	return ResponseEntity.ok(TableListResponse.builder().tableList(tableList).build());
    }

	@PostMapping("/uploadFile")
	public ResponseEntity<FileTransferResponse> uploadFile(MultipartFile csvFile, String tableName, Boolean truncateTable) throws IOException {
    	LOGGER.info(BEGIN);
    	LOGGER.info("Received {}, size {} bytes", csvFile.getOriginalFilename(), csvFile.getSize());
    	LOGGER.info("tableName: {}", tableName);
    	LOGGER.info("truncateTable: {}", truncateTable);

    	var tableEnum = tableEnumFromString(tableName);
			var fileTransferResponse = genericFileTransferService.parseAndSave(tableEnum, csvFile.getOriginalFilename(),
					csvFile.getInputStream(), truncateTable);
    		LOGGER.info(END);
        	return ResponseEntity.ok(fileTransferResponse);
    }

    @GetMapping("/downloadExceptionsFile")
	public ResponseEntity<byte[]> downloadExceptionsFile(@RequestParam(value = "exceptionsFile") @Valid String exceptionsFile) throws IOException {
    	LOGGER.info(BEGIN);
    	var exceptionsFileFullPath = Path.of(FileUtils.getTempDirectoryPath() ,exceptionsFile);
    	LOGGER.info("Downloading file: {}", exceptionsFileFullPath);
    	var fileBytes = FileUtils.readFileToByteArray(exceptionsFileFullPath.toFile());
		LOGGER.info(END);
    	return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "exceptionsFile-"+AppTimeUtils.getNowString()+".csv")
    	        .contentType(MediaType.parseMediaType("application/csv")).body(fileBytes);
    }

	@GetMapping("/downloadFile")
	public ResponseEntity<byte[]> downloadFile(String tableName) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IllegalArgumentException, NoSuchFieldException, SecurityException {
    	LOGGER.info(BEGIN);
    	var fileBytes = genericFileTransferService.readAndWrite(tableEnumFromString(tableName));
		
		LOGGER.info(END);
    	return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tableName.toLowerCase()+".csv")
    	        .contentType(MediaType.parseMediaType("application/csv")).body(fileBytes);
    }
	
	private IEntityEnum tableEnumFromString(String tableName) {
    	var upperTableName = tableName.toUpperCase();
		return ObjectUtils.defaultIfNull(UploadTableEnum.valueIfPresent(upperTableName),
				InvestmentPortfolioTableEnum.valueIfPresent(upperTableName));
	}
}
