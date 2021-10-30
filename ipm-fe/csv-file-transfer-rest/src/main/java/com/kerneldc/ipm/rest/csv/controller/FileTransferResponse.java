package com.kerneldc.ipm.rest.csv.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kerneldc.ipm.rest.csv.service.ProcessingStats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileTransferResponse {

	private String transformerExceptionMessage;
	@JsonUnwrapped
	private ProcessingStats processingStats;
	private String exceptionsFileName;
}
