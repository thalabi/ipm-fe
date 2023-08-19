package com.kerneldc.ipm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Portfolio;

public interface PortfolioRepository extends BaseTableRepository<Portfolio, Long> {
	
	@Query(value = """
			select p.id, p.lk, p.institution, p.account_number as accountNumber, p.name, p.currency, p.version,
				case when (select count(h.*) from holding h where h.portfolio_id = p.id) > 0 then true else false end as hasHoldings,
				case when (select count(po.*) from position po where po.portfolio_id = p.id) > 0 then true else false end as hasPositions
			from portfolio p
			""", nativeQuery = true)
	List<IPortfolioWithDependentFlags> findAllPortfoliosWithDependentFlags();

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PORTFOLIO;
	}
	
}
