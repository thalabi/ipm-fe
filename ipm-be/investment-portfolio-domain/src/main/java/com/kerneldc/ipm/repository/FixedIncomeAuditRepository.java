package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.FixedIncomeAudit;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface FixedIncomeAuditRepository extends BaseTableRepository<FixedIncomeAudit, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.FIXED_INCOME_AUDIT;
	}
	
}
