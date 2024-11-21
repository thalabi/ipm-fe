package com.kerneldc.ipm.repository;

import java.util.List;

import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InstrumentDueV;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;

public interface InstrumentDueVRepository extends BaseViewRepository<InstrumentDueV, Long> {
	
	List<InstrumentDueV> findByEmailNotificationOrderByDueDateAscIssuerFiAscTypeAscCurrencyAsc(Boolean emailNotification);
	List<InstrumentDueV> findByOrderByPortfolioFiAscPortfolioHolderAscCurrencyAscPortfolioNameAsc();
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioEntityEnum.INSTRUMENT_DUE_V;
	}

	
}
