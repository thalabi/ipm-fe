package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.FixedIncomeAudit;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;

public interface FixedIncomeAuditRepository extends BaseTableRepository<FixedIncomeAudit, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioEntityEnum.FIXED_INCOME_AUDIT;
	}
	
}
