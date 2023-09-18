package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportJobResponse {

	private String filename;
	private LocalDateTime timestamp;
}
