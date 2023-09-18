package com.kerneldc.ipm.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class AppFileUtils {

	public static final String TEMP_FILES_PREFIX = "ipm-application";

	private AppFileUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String getSeparator() {
		return FileSystems.getDefault().getSeparator();
	}
	
	public static Path createTempFile(String suffix) throws IOException{
		long threadId = Thread.currentThread().getId();
		return Files.createTempFile(TEMP_FILES_PREFIX+"-"+AppTimeUtils.getNowString()+"-"+threadId, suffix);
	}

	public static Path createTempFile() throws IOException {
		return createTempFile(null);
	}	
}
