package com.kerneldc.ipm.repository.service;

import javax.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ConcurrentRecordAccessException;
import com.kerneldc.common.exception.RecordIntegrityViolationException;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.repository.HoldingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingService {

	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private final HoldingRepository holdingRepository;

	/**
	 * This is a wrapper for the transactionalSave() method below.
	 * It's purpose to catch the DataIntegrityViolationException as it is only thrown after the transaction has completed.
	 * 
	 * @param holding
	 * @return
	 */
	public Holding save(Holding holding) {
		LOGGER.info(LOG_BEGIN);
		try {
	    	LOGGER.info(LOG_END);
			return transactionalSave(holding);
		} catch (DataIntegrityViolationException e) {
			LOGGER.error("save of record {} caused: ", holding, e);
			throw new RecordIntegrityViolationException(e);
		}
	}
	
	@Transactional
	private Holding transactionalSave(Holding holding) {
		LOGGER.info(LOG_BEGIN);
		LOGGER.info("holding: {}", holding);
		try {
			holdingRepository.save(holding);
		} catch (ObjectOptimisticLockingFailureException e) {
			LOGGER.error("save of record {} caused: ", holding, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.UPDATE_EXCEPTION_MESSAGE, e);
		}
    	LOGGER.info(LOG_END);
		return holding;
	}

	@Transactional
	public void delete(Long id) {
		LOGGER.info(LOG_BEGIN);
		try {
			holdingRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.error("delete of record id {} caused: ", id, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.DELETE_EXCEPTION_MESSAGE, e);
		}
		LOGGER.info(LOG_END);
	}

}
