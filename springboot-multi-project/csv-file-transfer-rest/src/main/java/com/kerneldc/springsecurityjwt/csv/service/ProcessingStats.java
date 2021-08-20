package com.kerneldc.springsecurityjwt.csv.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessingStats {

	private Integer numberOfLinesInFile;
	private Integer numberOfExceptions;
	private String elapsedTime;
	
	public void incrementExceptionsCounts(int count) {
		numberOfExceptions += count;
	}
}
