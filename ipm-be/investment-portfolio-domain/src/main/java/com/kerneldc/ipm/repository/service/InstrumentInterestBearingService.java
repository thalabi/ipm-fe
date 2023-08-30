package com.kerneldc.ipm.repository.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import javax.transaction.Transactional;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.kerneldc.common.exception.ConcurrentRecordAccessException;
import com.kerneldc.common.exception.RecordIntegrityViolationException;
import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentInterestBearingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentInterestBearingService {
	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private final InstrumentInterestBearingRepository instrumentInterestBearingRepository;
	
	/**
	 * This is a wrapper for the {@link #transactionalSave(InstrumentInterestBearing) transactionalSave} method.
	 * It's purpose to catch the DataIntegrityViolationException as it is only thrown after the transaction has completed.
	 * 
	 * @param holding
	 * @return
	 */
	public InstrumentInterestBearing save(InstrumentInterestBearing iib) {
		LOGGER.info(LOG_BEGIN);
		try {
			transactionalSave(iib);
		} catch (DataIntegrityViolationException e) {
			LOGGER.error("save of record {} caused: ", iib, e);
			throw new RecordIntegrityViolationException(e);
		}
		LOGGER.info(LOG_END);
		return iib;
	}

	/**
	 * @param iib
	 * @return
	 */
	@Transactional
	private InstrumentInterestBearing transactionalSave(InstrumentInterestBearing iib) {
		LOGGER.info(LOG_BEGIN);
		LOGGER.info("iib: {}", iib);
		var i = iib.getInstrument();
		i.setTicker(getTicker(iib.getType(), i.getName(), i.getTicker()));
		iib.setPrice(setDefaultPrice(iib.getPrice()));
		try {
			instrumentInterestBearingRepository.save(iib);
		} catch (ObjectOptimisticLockingFailureException e) {
			LOGGER.error("save of record {} caused: ", iib, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.UPDATE_EXCEPTION_MESSAGE, e);
		}
		LOGGER.info(LOG_END);
		return iib;
	}
	
	@Transactional
	public void delete(Long id) {
		LOGGER.info(LOG_BEGIN);
		try {
			instrumentInterestBearingRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.error("delete of record id {} caused: ", id, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.DELETE_EXCEPTION_MESSAGE, e);
		}
		LOGGER.info(LOG_END);
	}

    private String getTicker(InterestBearingTypeEnum type, String name, String ticker) {
		if (Arrays.asList(InterestBearingTypeEnum.MONEY_MARKET, InterestBearingTypeEnum.INVESTMENT_SAVINGS)
				.contains(type)) {
			return ticker;
    	} else {
    		// For InterestBearingTypeEnum CHEQUING,SAVINGS, GIC and TERM_DEPOSIT use the instrument name to generate a hash as the ticker
    		return md5(name);
    	}
    }

    private static String md5(String name) {
    	var hashBytes = DigestUtils.md5Digest(name.getBytes());
    	// Remove null bytes from hash because postgres does not allow nulls in varchar
    	var noNullHashBytesList = new ArrayList<Byte>();
    	for (byte hashByte: hashBytes) {
    		if (hashByte == 0) continue;
    		noNullHashBytesList.add(hashByte);
    	}
    	var noNullHashBytesArray = noNullHashBytesList.toArray(new Byte[noNullHashBytesList.size()]);
    	return new String(ArrayUtils.toPrimitive(noNullHashBytesArray), StandardCharsets.UTF_8);
    }

    private BigDecimal setDefaultPrice(BigDecimal price) {
    	return price == null || price.equals(BigDecimal.ZERO) ? BigDecimal.ONE : price;
    }
}
