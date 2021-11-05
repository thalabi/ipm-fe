package com.kerneldc.ipm.rest.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kerneldc.common.repository.AreaCodeRepository;
import com.kerneldc.ipm.batch.ExchangeRateService;
import com.kerneldc.ipm.batch.HoldingPricingService;
import com.kerneldc.ipm.repository.ExchangeRateRepository;
import com.kerneldc.ipm.repository.HoldingPriceInterdayVRepository;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PriceRepository;
import com.kerneldc.ipm.util.EmailService;
import com.kerneldc.ipm.util.SmsService;


@Configuration
@EnableScheduling
public class InvestmentPortfolioBatchBeans {

	@Bean
	public SmsService getSmsService(freemarker.template.Configuration freeMarkerConfiguration, EmailService emailService, AreaCodeRepository areaCodeRepository) {
		return new SmsService(freeMarkerConfiguration, emailService, areaCodeRepository);
	}
	@Bean
	public EmailService getEmailService(JavaMailSender emailSender, freemarker.template.Configuration freeMarkerConfiguration, @Value("${application.security.jwt.token.resetPasswordJwtExpiryInMinutes:60}" /* default of 1 hour */) int resetPasswordJwtExpiryInMinutes) {
		return new EmailService(emailSender, freeMarkerConfiguration, resetPasswordJwtExpiryInMinutes);
	}
	@Bean
	public HoldingPricingService getHoldingPricingService(HoldingRepository holdingRepository,
			PositionRepository positionRepository, PriceRepository priceRepository,
			ExchangeRateRepository exchangeRateRepository, HoldingPriceInterdayVRepository holdingPriceInterdayVRepository,
			EmailService emailService) {
		return new HoldingPricingService(holdingRepository, positionRepository, priceRepository, holdingPriceInterdayVRepository,
				new ExchangeRateService(exchangeRateRepository), emailService);
	}
}
