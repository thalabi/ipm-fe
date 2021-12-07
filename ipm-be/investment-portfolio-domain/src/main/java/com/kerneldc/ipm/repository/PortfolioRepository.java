package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Portfolio;

public interface PortfolioRepository extends BaseTableRepository<Portfolio, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PORTFOLIO;
	}
	
}
