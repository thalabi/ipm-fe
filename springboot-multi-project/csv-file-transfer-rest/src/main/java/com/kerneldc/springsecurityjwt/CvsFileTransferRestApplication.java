package com.kerneldc.springsecurityjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication//(scanBasePackages = "com.kerneldc")
@EntityScan (basePackages = {"com.kerneldc"})
@EnableJpaRepositories({"com.kerneldc.springsecurityjwt.security.repository","com.kerneldc.springsecurityjwt.csv.repository","com.kerneldc.portfolio.repository"})
public class CvsFileTransferRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvsFileTransferRestApplication.class, args);
	}

}