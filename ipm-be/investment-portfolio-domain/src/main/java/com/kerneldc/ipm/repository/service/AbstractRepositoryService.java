package com.kerneldc.ipm.repository.service;

import javax.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.kerneldc.common.exception.ConcurrentRecordAccessException;
import com.kerneldc.common.exception.RecordIntegrityViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRepositoryService<T, I> {

	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private final JpaRepository<T, I> holdingRepository;

	/**
	 * This is a wrapper for the {@link  transactionalSave transactionalSave} method below.
	 * It's purpose to catch the DataIntegrityViolationException as it is only thrown after the transaction has completed.
	 * 
	 * @param entity
	 * @return
	 */
	public T save(T entity) {
		LOGGER.info(LOG_BEGIN);
		try {
			transactionalSave(entity);
		} catch (DataIntegrityViolationException e) {
			LOGGER.error("save of record {} caused: ", entity, e);
			throw new RecordIntegrityViolationException(e);
		}
		LOGGER.info(LOG_END);
		return entity;
	}
	

	@Transactional
	private T transactionalSave(T entity) {
		LOGGER.info(LOG_BEGIN);
		LOGGER.info("entity: {}", entity);
		handleEntity(entity);
		try {
			holdingRepository.save(entity);
		} catch (ObjectOptimisticLockingFailureException e) {
			LOGGER.error("save of entity {} caused: ", entity, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.UPDATE_EXCEPTION_MESSAGE, e);
		}
    	LOGGER.info(LOG_END);
		return entity;
	}

	protected abstract void handleEntity(T entity);

	@Transactional
	public void delete(I id) {
		LOGGER.info(LOG_BEGIN);
		try {
			holdingRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.error("delete of entity id {} caused: ", id, e);
			throw new ConcurrentRecordAccessException(ConcurrentRecordAccessException.DELETE_EXCEPTION_MESSAGE, e);
		}
		LOGGER.info(LOG_END);
	}

}
