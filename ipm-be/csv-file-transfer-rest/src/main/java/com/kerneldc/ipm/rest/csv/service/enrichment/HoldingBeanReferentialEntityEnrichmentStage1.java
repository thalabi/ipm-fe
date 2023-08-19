package com.kerneldc.ipm.rest.csv.service.enrichment;

import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.rest.csv.service.enrichment.BeanReferentialEntityEnrichmentService.BeanReferentialEntityEnrichmentResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HoldingBeanReferentialEntityEnrichmentStage1 implements IBeanReferentialEntityEnrichment {


	@Override
	public BeanReferentialEntityEnrichmentResult enrich(BeanReferentialEntityEnrichmentResult beanReferentialEntityEnrichmentResultList) throws EnrichmentException {
		var beanList = beanReferentialEntityEnrichmentResultList.beanList();
		for (AbstractEntity bean: beanList) {
			var holding = (Holding) bean;
			
			holding.setTicker(holding.getInstrument().getTicker());
			//holding.setExchange(holding.getInstrument().getExchange());
			
			holding.setAccountNumber(holding.getPortfolio().getAccountNumber());
			holding.setFinancialInstitution(holding.getPortfolio().getFinancialInstitution());
		}
		return beanReferentialEntityEnrichmentResultList;
	}

	@Override
	public boolean canHandle(IEntityEnum entityEnum, EnrichmentStageEnum enrichmentStageEnum) {
		return entityEnum.equals(InvestmentPortfolioTableEnum.HOLDING)
				&& enrichmentStageEnum.equals(EnrichmentStageEnum.STAGE_ONE);
	}

}
