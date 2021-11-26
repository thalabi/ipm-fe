package com.kerneldc.ipm.rest.csv.service.enrichment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeanReferentialEntityEnrichmentService {

	private final Collection<IBeanReferentialEntityEnrichment> enrichmentCollection;
	
	public record BeanReferentialEntityEnrichmentResult(List<? extends AbstractEntity> beanList, List<EnrichmentException> enrichmentExceptionList) {};
	
	public BeanReferentialEntityEnrichmentResult applyEnrichers(IEntityEnum uploadTabelEnum, List<? extends AbstractEntity> beanList) {
		
		var enrichrmentResult = new BeanReferentialEntityEnrichmentResult(beanList, new ArrayList<>());
		
		for (IBeanReferentialEntityEnrichment enricher: enrichmentCollection) {
			for (EnrichmentStageEnum stage : EnrichmentStageEnum.values())
				if (enricher.canHandle(uploadTabelEnum, stage)) {
					try {
						LOGGER.info("Using {} to enrich bean of {} table with referential entity keys.", enricher.getClass().getSimpleName(), uploadTabelEnum);
						enrichrmentResult = enricher.enrich(enrichrmentResult);
					} catch (EnrichmentException e) {
						enrichrmentResult.enrichmentExceptionList().add(e);
						enrichrmentResult = new BeanReferentialEntityEnrichmentResult(enrichrmentResult.beanList(), enrichrmentResult.enrichmentExceptionList());
					}
				}
		}
		return enrichrmentResult;
	}

}
