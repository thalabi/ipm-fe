package com.kerneldc.springsecurityjwt.csv.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kerneldc.springsecurityjwt.csv.service.ProcessingStats;

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
