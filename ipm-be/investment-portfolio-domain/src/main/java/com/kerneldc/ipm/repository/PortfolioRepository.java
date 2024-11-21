package com.kerneldc.ipm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;
import com.kerneldc.ipm.domain.Portfolio;

public interface PortfolioRepository extends BaseTableRepository<Portfolio, Long> {
	
	@Query(value = """
			select p.id, p.financial_institution as financialInstitutionNumber, p.name, p.holder, p.portfolio_id as portfolioId, p.currency, p.logically_deleted as logicallyDeleted,
				case when (select count(h.*) from holding h where h.portfolio_id = p.id) > 0 then true else false end as hasHoldings,
				case when (select count(po.*) from position po where po.portfolio_id = p.id) > 0 then true else false end as hasPositions,
				p.version
			from portfolio p
			order by p.financial_institution, p.name
			""", nativeQuery = true)
	List<IPortfolioWithDependentFlags> findAllPortfoliosWithDependentFlags();

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioEntityEnum.PORTFOLIO;
	}
	
}
