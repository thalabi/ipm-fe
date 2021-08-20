package com.kerneldc.portfolio.repository;

import java.time.LocalDate;
import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.CurrencyEnum;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.portfolio.domain.ExchangeRate;
import com.kerneldc.portfolio.domain.InvestmentPortfolioTableEnum;

public interface ExchangeRateRepository extends BaseTableRepository<ExchangeRate, Long> {
	
	List<ExchangeRate> findByDateAndFromCurrencyAndToCurrency(LocalDate date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency);
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.EXCHANGE_RATE;
	}
	
}
