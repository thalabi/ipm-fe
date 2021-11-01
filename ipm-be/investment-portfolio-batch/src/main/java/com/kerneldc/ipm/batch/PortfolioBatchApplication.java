package com.kerneldc.ipm.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)//(scanBasePackages = "com.kerneldc")
@EntityScan (basePackages = {"com.kerneldc"})
@EnableJpaRepositories({"com.kerneldc.springsecurityjwt.security.repository","com.kerneldc.springsecurityjwt.csv.repository","com.kerneldc.ipm.repository"})
public class PortfolioBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioBatchApplication.class, args);
		
		// HoldingPricingService.run() will be executed when app starts
	}

}