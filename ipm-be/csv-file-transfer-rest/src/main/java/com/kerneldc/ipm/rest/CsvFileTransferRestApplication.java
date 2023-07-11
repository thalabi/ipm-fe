package com.kerneldc.ipm.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.kerneldc") // need to specify scanBasePackages in multi module project
@EntityScan(basePackages = {"com.kerneldc"})
@EnableJpaRepositories(basePackages = {"com.kerneldc"})
@EnableScheduling
public class CsvFileTransferRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvFileTransferRestApplication.class, args);
	}

}