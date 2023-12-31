package com.kerneldc.ipm.repository.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMutualFund;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstrumentMutualFundRepositoryService extends AbstractRepositoryService<InstrumentMutualFund, Long>{
	
	public InstrumentMutualFundRepositoryService(JpaRepository<InstrumentMutualFund, Long> instrumentMutualFundRepository) {
		super(instrumentMutualFundRepository);
	}

	@Override
	protected void handleEntity(InstrumentMutualFund imf) {
		LOGGER.info("imf: {}", imf);
		var i = imf.getInstrument();
		i.setType(InstrumentTypeEnum.MUTUAL_FUND);
	}
	
}
