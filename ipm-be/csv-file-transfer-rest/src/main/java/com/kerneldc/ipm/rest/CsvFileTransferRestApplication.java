package com.kerneldc.ipm.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.kerneldc") // need to specify scanBasePackages in multi module project
@EntityScan(basePackages = {"com.kerneldc"})
@EnableJpaRepositories(basePackages = {"com.kerneldc"})
public class CsvFileTransferRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvFileTransferRestApplication.class, args);
	}

}