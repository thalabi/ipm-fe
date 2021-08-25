package com.kerneldc.common;

import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.NoRepositoryBean;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;

@NoRepositoryBean
public interface BaseTableRepository<T extends AbstractPersistableEntity, ID extends Serializable> extends BaseEntityRepository<T, ID> {	

	Long deleteByLogicalKeyHolder(LogicalKeyHolder logicalKeyHolder);
	
	@Transactional
	default <E extends T> void deleteListByLogicalKeyHolder(List<E> entities) {
		entities.forEach(entity -> deleteByLogicalKeyHolder(entity.getLogicalKeyHolder()));
	}
	
	@Transactional
	default <E extends T> List<E> saveAllTransaction(Iterable<E> entities) {
		return saveAll(entities);
	}
	
	@Transactional
	default <E extends T> E saveTransaction(E entity) {
		return save(entity);
	}
}
