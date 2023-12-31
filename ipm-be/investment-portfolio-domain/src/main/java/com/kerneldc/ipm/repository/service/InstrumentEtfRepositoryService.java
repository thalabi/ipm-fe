package com.kerneldc.ipm.repository.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentEtf;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstrumentEtfRepositoryService extends AbstractRepositoryService<InstrumentEtf, Long>{
	
	public InstrumentEtfRepositoryService(JpaRepository<InstrumentEtf, Long> InstrumentEtfRepository) {
		super(InstrumentEtfRepository);
	}

	@Override
	protected void handleEntity(InstrumentEtf ietf) {
		LOGGER.info("ietf: {}", ietf);
		var i = ietf.getInstrument();
		i.setType(InstrumentTypeEnum.ETF);
	}
	
}
