package com.kerneldc.ipm.repository.service;

import javax.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ConcurrentRecordAccessException;
import com.kerneldc.common.exception.RecordIntegrityViolationException;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {
	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private final PortfolioRepository portfolioRepository;
	
	/**
	 * This is a wrapper for the {@link #transactionalSave(Portfolio) transactionalSave} method.
	 * It's purpose to catch the DataIntegrityViolationException as it is only thrown after the transaction has completed.
	 * 
	 * @param holding
	 * @return
	 */
	public Portfolio save(Portfolio iib) {
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
	 * @param portfolio
	 * @return
	 */
	@Transactional
	private Portfolio transactionalSave(Portfolio portfolio) {
		LOGGER.info(LOG_BEGIN);
		LOGGER.info("portfolio: {}", portfolio);
		try {
			portfolioRepository.save(portfolio);
		} catch (ObjectOptimisticLockingFailureException e) {
			LOGGER.error("save of record {} caused: ", portfolio, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.UPDATE_EXCEPTION_MESSAGE, e);
		}
		LOGGER.info(LOG_END);
		return portfolio;
	}
	
	@Transactional
	public void delete(Long id) {
		LOGGER.info(LOG_BEGIN);
		try {
			portfolioRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.error("delete of record id {} caused: ", id, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.DELETE_EXCEPTION_MESSAGE, e);
		}
		LOGGER.info(LOG_END);
	}
}
