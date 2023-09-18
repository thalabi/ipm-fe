package com.kerneldc.ipm.batch;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.InstrumentDueV;
import com.kerneldc.ipm.repository.InstrumentDueVRepository;
import com.kerneldc.ipm.util.EmailService;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentDueNotificationService {
	private final EmailService emailService;
	private final InstrumentDueVRepository instrumentDueVRepository;
	@Value("${instrument.due.days.to.notify:7}")
	private Long daysToNotify;
	
	private OffsetDateTime now;
	
	public void checkDueDate(Long daysToNotify) throws ApplicationException {
		LOGGER.info("Begin ...");
		LOGGER.info("daysToNotify: {}", daysToNotify);
		Preconditions.checkArgument(daysToNotify > 0l, "daysToNotify is %s, Days to notify cannot be negative.", daysToNotify);
		now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		var instrumentDueVList = instrumentDueVRepository.findByEmailNotificationOrderByDueDateAscIssuerFiAscTypeAscCurrencyAsc(true);
		if (instrumentDueVList.isEmpty()) {
			return;
		}
		
		List<InstrumentDueV> instrumentDueVtoNotifyList = new ArrayList<>();
		for (var instrumentDueV : instrumentDueVList) {
			if (instrumentDueV.getDueDate() == null) {
				continue;
			}
			Long numberOfDays = AppTimeUtils.daysBetween(now, instrumentDueV.getDueDate());
			LOGGER.debug("daysBetween({}, {}) = {}", now, instrumentDueV.getDueDate(), numberOfDays);
			if (numberOfDays.compareTo(daysToNotify) <= 0) {
				
				setDateOverdue(instrumentDueV, numberOfDays);
				setFinancialInstitutionName(instrumentDueV);
				
				instrumentDueVtoNotifyList.add(instrumentDueV);
				LOGGER.debug("Notify that this instrument is due: {}", instrumentDueV);
			}
		}
		if (! /* not */ instrumentDueVtoNotifyList.isEmpty()) {
			var overdueInstrument = instrumentDueVtoNotifyList.stream().filter(InstrumentDueV::getOverdue).findFirst();
			emailService.sendInstrumentDueNotification(daysToNotify, instrumentDueVtoNotifyList, overdueInstrument.isPresent());
		}
		LOGGER.info("instrumentDueVtoNotifyList size: {}", instrumentDueVtoNotifyList.size());
		LOGGER.info("End ...");		
	}

	private void setDateOverdue(InstrumentDueV instrumentDueV, Long numberOfDays) {
		instrumentDueV.setOverdue(numberOfDays < 0l);
	}
	private void setFinancialInstitutionName(InstrumentDueV instrumentDueV) {
		if (instrumentDueV.getPortfolioFi() != null) {
			instrumentDueV.setPortfolioFiName(FinancialInstitutionEnum.financialInstitutionEnumOf(instrumentDueV.getPortfolioFi()).name());
		}
		if (instrumentDueV.getIssuerFi() != null) {
			instrumentDueV.setIssuerFiName(FinancialInstitutionEnum.financialInstitutionEnumOf(instrumentDueV.getIssuerFi()).name());
		}
	}

	public void checkDueDate() throws ApplicationException {
		checkDueDate(daysToNotify);
	}
}
