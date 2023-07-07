package com.kerneldc.ipm.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kerneldc")
//@EntityScan (basePackages = {"com.kerneldc"})
public class CsvFileTransferRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvFileTransferRestApplication.class, args);
	}

}