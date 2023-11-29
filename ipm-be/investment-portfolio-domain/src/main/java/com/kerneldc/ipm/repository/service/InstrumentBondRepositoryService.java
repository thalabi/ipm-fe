package com.kerneldc.ipm.repository.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentBond;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstrumentBondRepositoryService extends AbstractRepositoryService<InstrumentBond, Long>{
	
	public InstrumentBondRepositoryService(JpaRepository<InstrumentBond, Long> instrumentBondRepository) {
		super(instrumentBondRepository);
	}

	@Override
	protected void handleEntity(InstrumentBond ib) {
		LOGGER.info("ib: {}", ib);
		var i = ib.getInstrument();
		i.setTicker(ib.getCusip());
		i.setType(InstrumentTypeEnum.BOND);
	}
	
}
