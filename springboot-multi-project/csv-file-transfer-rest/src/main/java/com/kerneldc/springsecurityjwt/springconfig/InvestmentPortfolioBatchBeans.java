package com.kerneldc.springsecurityjwt.springconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kerneldc.portfolio.batch.ExchangeRateService;
import com.kerneldc.portfolio.batch.HoldingPricingService;
import com.kerneldc.portfolio.repository.ExchangeRateRepository;
import com.kerneldc.portfolio.repository.HoldingRepository;
import com.kerneldc.portfolio.repository.PositionRepository;

@Configuration
@EnableScheduling
public class InvestmentPortfolioBatchBeans {

	@Autowired
	private HoldingRepository holdingRepository;
	@Autowired
	private PositionRepository positionRepository;
	@Autowired
	private ExchangeRateRepository exchangeRateRepository;
	@Bean
	public HoldingPricingService getHoldingPricingService() {
		return new HoldingPricingService(holdingRepository, positionRepository, new ExchangeRateService(exchangeRateRepository));
	}
}
