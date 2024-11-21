package com.kerneldc.ipm.rest.csv.service.enrichment;

import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;
import com.kerneldc.ipm.domain.Position;
import com.kerneldc.ipm.rest.csv.service.enrichment.BeanReferentialEntityEnrichmentService.BeanReferentialEntityEnrichmentResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PositionBeanReferentialEntityEnrichmentStage1 implements IBeanReferentialEntityEnrichment {


	@Override
	public BeanReferentialEntityEnrichmentResult enrich(BeanReferentialEntityEnrichmentResult beanReferentialEntityEnrichmentResultList) throws EnrichmentException {
		var beanList = beanReferentialEntityEnrichmentResultList.beanList();
		for (AbstractEntity bean: beanList) {
			var position = (Position) bean;
			
			position.setTicker(position.getInstrument().getTicker());
			//position.setExchange(position.getInstrument().getExchange());
			
			position.setAccountNumber(position.getPortfolio().getPortfolioId());
			position.setFinancialInstitution(position.getPortfolio().getFinancialInstitution());

			position.setPriceTimestamp(position.getPrice().getPriceTimestamp());

		}
		return beanReferentialEntityEnrichmentResultList;
	}

	@Override
	public boolean canHandle(IEntityEnum entityEnum, EnrichmentStageEnum enrichmentStageEnum) {
		return entityEnum.equals(InvestmentPortfolioEntityEnum.POSITION)
				&& enrichmentStageEnum.equals(EnrichmentStageEnum.STAGE_ONE);
	}

}
