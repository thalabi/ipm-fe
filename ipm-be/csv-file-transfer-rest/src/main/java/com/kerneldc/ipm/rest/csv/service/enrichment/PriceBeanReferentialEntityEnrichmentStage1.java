package com.kerneldc.ipm.rest.csv.service.enrichment;

import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.rest.csv.service.enrichment.BeanReferentialEntityEnrichmentService.BeanReferentialEntityEnrichmentResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PriceBeanReferentialEntityEnrichmentStage1 implements IBeanReferentialEntityEnrichment {


	@Override
	public BeanReferentialEntityEnrichmentResult enrich(BeanReferentialEntityEnrichmentResult beanReferentialEntityEnrichmentResultList) throws EnrichmentException {
		var beanList = beanReferentialEntityEnrichmentResultList.beanList();
		for (AbstractEntity bean: beanList) {
			var price = (Price) bean;
			
			price.setTicker(price.getInstrument().getTicker());
			price.setExchange(price.getInstrument().getExchange());
		}
		return beanReferentialEntityEnrichmentResultList;
	}

	@Override
	public boolean canHandle(IEntityEnum entityEnum, EnrichmentStageEnum enrichmentStageEnum) {
		return entityEnum.equals(InvestmentPortfolioTableEnum.PRICE)
				&& enrichmentStageEnum.equals(EnrichmentStageEnum.STAGE_ONE);
	}

}
