package com.kerneldc.ipm.batch;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.InstrumentDueV;
import com.kerneldc.ipm.repository.InstrumentDueVRepository;
import com.kerneldc.ipm.util.EmailService;
import com.kerneldc.ipm.util.TimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentDueNotificationService {
	private final EmailService emailService;
	private final InstrumentDueVRepository instrumentInterestBearingRepository;
	@Value("${instrument.due.days.to.notify:7}")
	private Long daysToNotify;
	
	private OffsetDateTime now;
	
	public void checkDueDate() throws ApplicationException {
		LOGGER.info("daysToNotify: {}", daysToNotify);
		now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		var instrumentDueVList = instrumentInterestBearingRepository.findByEmailNotification(true);
		if (instrumentDueVList.isEmpty()) {
			return;
		}
		
		List<InstrumentDueV> instrumentDueVtoNotifyList = new ArrayList<>();
		for (var instrumentDueV : instrumentDueVList) {
			if (instrumentDueV.getDueDate() == null) {
				continue;
			}
			var dueDate = TimeUtils.offsetDateTimeFromDateString(instrumentDueV.getDueDate(), DateTimeFormatter.ofPattern("uuuu-MM-dd"));
			Long numberOfDays = TimeUtils.daysBetween(now, dueDate);
			LOGGER.debug("daysBetween({}, {}) = {}", now, dueDate, numberOfDays);
			if (numberOfDays.compareTo(daysToNotify) <= 0) {
				instrumentDueVtoNotifyList.add(instrumentDueV);
				LOGGER.debug("Notify that this instrument is due: {}", instrumentDueV);
			}
		}
		if (! /* not */ instrumentDueVtoNotifyList.isEmpty()) {
			emailService.sendInstrumentDueNotification(instrumentDueVtoNotifyList);
		}
		LOGGER.info("instrumentDueVtoNotifyList size: {}", instrumentDueVtoNotifyList.size());
	}
}
