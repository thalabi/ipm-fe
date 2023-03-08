package com.kerneldc.ipm.rest.csv.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessingStats {

	private Long numberOfLinesInFile;
	private Integer numberOfExceptions;
	private String elapsedTime;
}
