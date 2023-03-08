package com.kerneldc.ipm.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class OutputToInputStreamTest {

	@TempDir
	private static Path tempDir;
	
	private static Path dataPath;

	@BeforeAll
	static void init() throws IOException {
		dataPath = tempDir.resolve("data.dat");
		
		LOGGER.info("dataPath: {}", dataPath);
		dataPath.toFile().createNewFile();
		Files.writeString(dataPath, "data file", StandardOpenOption.CREATE);
	}

	@Test
	void test1() throws IOException {
		
		var transformedDataPath = transformService(dataPath);
		
		var outDataPathContent= Files.readString(transformedDataPath);
		LOGGER.info("outDataPathContent: {}", outDataPathContent);
	}
	
	private Path transformService(Path inputFilePath) throws IOException {
		var tempFile = Files.createTempFile(null, null);
		LOGGER.info("tempFile: {}", tempFile);
		
		Files.copy(inputFilePath, tempFile, StandardCopyOption.REPLACE_EXISTING);
		
		// transformer 1
		var workTempFile = tempFile;
		workTempFile = transform(workTempFile, "transformer 1");
		workTempFile = transform(workTempFile, "transformer 2");
		workTempFile = transform(workTempFile, "transformer 3");
		
		return workTempFile;
	}
	private Path transform(Path inputFilePath, String text) throws IOException {
		var inputFileContent= Files.readString(inputFilePath);

		var transformerTempFile = Files.createTempFile(null, null);
		LOGGER.info("transformerTempFile: {}", transformerTempFile);
		Files.writeString(transformerTempFile, (StringUtils.isEmpty(inputFileContent) ? text : inputFileContent+" "+text), StandardOpenOption.WRITE);
		return transformerTempFile;
	}
}
