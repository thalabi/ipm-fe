package com.kerneldc.ipm.domain.listener;

import java.util.List;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentBond;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.repository.FixedIncomeAuditRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Entity Listener for InstrumentBond and InstrumentInterestBearing.
 */
@Component
@Slf4j
public class FixedIncomeListener {

	private final FixedIncomeAuditRepository fixedIncomeAuditRepository;
	
	// Use @Lazy to avoid circular dependencies
	public FixedIncomeListener(@Lazy FixedIncomeAuditRepository fixedIncomeAuditRepository) {
		this.fixedIncomeAuditRepository = fixedIncomeAuditRepository;
	}

	@PostPersist
    @PostUpdate
    @PostRemove
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void afterAnyUpdate(AbstractPersistableEntity entity) {
    	LOGGER.info("An insert/update/delete completed for {}: {}", entity.getClass().getSimpleName(), entity.getId());

    	if (entity instanceof InstrumentInterestBearing || entity instanceof InstrumentBond) {
    		updateFixedIncomeAudit();
    	} else if (entity instanceof Holding holding) {
        	if (List.of(InstrumentTypeEnum.INTEREST_BEARING, InstrumentTypeEnum.BOND).contains(holding.getInstrument().getType())) {
        		LOGGER.info("Associated instrument type is fixed income.");
        		
        		updateFixedIncomeAudit();
        		
        	} else {
        		LOGGER.info("Associated instrument type is not fixed income.");
        	}

    	} else {
    		throw new IllegalStateException(String.format("FixedIncomeListener is not designed to handle %s entities.", entity.getClass().getSimpleName()));
    	}
	}

	private void updateFixedIncomeAudit() {
		LOGGER.info("Noting a change to fixed income instruments or portfolios");
		var fixedIncomeAudit = fixedIncomeAuditRepository.findById(1l)
				.orElseThrow(() -> new IllegalStateException("Entity fixedIncomeAudit does not have a row with id 1."));
    	fixedIncomeAudit.setChange(true);
    	fixedIncomeAuditRepository.save(fixedIncomeAudit);

	}

}
