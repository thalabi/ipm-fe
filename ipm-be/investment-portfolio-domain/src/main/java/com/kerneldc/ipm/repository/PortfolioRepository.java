package com.kerneldc.ipm.repository;

import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Portfolio;

public interface PortfolioRepository extends BaseTableRepository<Portfolio, Long> {
	
	List<Portfolio> findByinstitutionAndAccountNumber(String institution, String accountNumber);
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PORTFOLIO;
	}
	
}
