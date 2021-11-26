package com.kerneldc.ipm.rest.csv.service.enrichment;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.service.enrichment.BeanReferentialEntityEnrichmentService.BeanReferentialEntityEnrichmentResult;

public interface IBeanReferentialEntityEnrichment {

	BeanReferentialEntityEnrichmentResult enrich(BeanReferentialEntityEnrichmentResult beanReferentialEntityEnrichmentResultList) throws EnrichmentException;
	
	boolean canHandle(IEntityEnum entityEnum, EnrichmentStageEnum enrichmentStageEnum);
}
