package com.kerneldc.ipm.batch;

import org.springframework.boot.SpringApplication;

//@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)//(scanBasePackages = "com.kerneldc")
//@EntityScan (basePackages = {"com.kerneldc"})
//@EnableJpaRepositories//({"com.kerneldc.ipm.repository"})
public class PortfolioBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioBatchApplication.class, args);
		
		// HoldingPricingService.run() will be executed when app starts
	}

}