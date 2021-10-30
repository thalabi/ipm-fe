package com.kerneldc.ipm.rest.springconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kerneldc.ipm.batch.ExchangeRateService;
import com.kerneldc.ipm.batch.HoldingPricingService;
import com.kerneldc.ipm.repository.ExchangeRateRepository;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PriceRepository;

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
