package com.kerneldc.ipm.rest.springconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling
public class InvestmentPortfolioBatchBeans {

//	@Value("${alphavantage.api.key}")
//	private String alphavantageApiKey;
//	@Bean
//	public EmailService emailServiceBean(JavaMailSender emailSender, freemarker.template.Configuration freeMarkerConfiguration) {
//		return new EmailService(emailSender, freeMarkerConfiguration);
//	}
//	@Bean
//	public EntityRepositoryFactory entityRepositoryFactoryBean(
//			Collection<BaseEntityRepository<? extends AbstractEntity, ? extends Serializable>> baseEntityRepositories,
//			Collection<BaseInstrumentDetailRepository<? extends AbstractEntity, ? extends Serializable>> baseInstrumentDetailRepositories) {
//		return new EntityRepositoryFactory(baseEntityRepositories, baseInstrumentDetailRepositories);
//	}
//	@Bean
//	public EntityRepositoryFactoryHelper entityRepositoryFactoryHelperBean(EntityRepositoryFactory entityRepositoryFactory) {
//		return new EntityRepositoryFactoryHelper(entityRepositoryFactory);
//	}
//	@Bean
//	public HoldingPricingService holdingPricingServiceBean(EntityRepositoryFactory entityRepositoryFactory,
//			EntityRepositoryFactoryHelper entityRepositoryFactoryHelper,
//			Collection<IInstrumentPricingService> pricingServiceCollection,
//			ExchangeRateRepository exchangeRateRepository, PriceRepository priceRepository,
//			EmailService emailService) {
//		return new HoldingPricingService(entityRepositoryFactory, entityRepositoryFactoryHelper, 
//				new ExchangeRateService(exchangeRateRepository), pricingServiceCollection, new MutualFundPriceService(priceRepository), new StockAndEtfPriceService(priceRepository, alphavantageApiKey), emailService);
//	}
}
