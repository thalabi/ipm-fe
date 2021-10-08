package com.kerneldc.springsecurityjwt.springconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kerneldc.portfolio.batch.ExchangeRateService;
import com.kerneldc.portfolio.batch.HoldingPricingService;
import com.kerneldc.portfolio.repository.ExchangeRateRepository;
import com.kerneldc.portfolio.repository.HoldingRepository;
import com.kerneldc.portfolio.repository.PositionRepository;
import com.kerneldc.portfolio.repository.PriceRepository;

@Configuration
@EnableScheduling
public class InvestmentPortfolioBatchBeans {
	
	@Bean
	public HoldingPricingService getHoldingPricingService(HoldingRepository holdingRepository,
			PositionRepository positionRepository, PriceRepository priceRepository,
			ExchangeRateRepository exchangeRateRepository) {
		return new HoldingPricingService(holdingRepository, positionRepository, priceRepository,
				new ExchangeRateService(exchangeRateRepository));
	}
}
