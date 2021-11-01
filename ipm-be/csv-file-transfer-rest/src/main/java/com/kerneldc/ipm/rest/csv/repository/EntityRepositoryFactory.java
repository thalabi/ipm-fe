package com.kerneldc.ipm.rest.csv.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.kerneldc.common.BaseEntityRepository;
import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.domain.AbstractImmutableEntity;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EntityRepositoryFactory {

	private Map<IEntityEnum, BaseEntityRepository<? extends AbstractEntity, ? extends Serializable>> repositoryMap = new HashMap<>();

	public EntityRepositoryFactory(Collection<BaseEntityRepository<? extends AbstractEntity, ? extends Serializable>> repositories) {
		
		LOGGER.debug("Loading repositoryMap with:");
		repositoryMap = repositories.stream().collect(Collectors.toMap(BaseEntityRepository::canHandle, r -> r));
		repositoryMap.forEach((entityEnum, repository) -> LOGGER.debug("[{}, {}, {}]", entityEnum, entityEnum.getEntity().getSimpleName(), entityEnum.isImmutable() ? "immutable entity" : "mutable entity"));

	}
	
	@SuppressWarnings("unchecked")
	public BaseTableRepository<AbstractPersistableEntity, Serializable> getTableRepository(IEntityEnum entityEnum) {
		Preconditions.checkArgument(! /* not */ entityEnum.isImmutable(), "getTableRepository() called with %s as argument when argument is immutable", entityEnum);
		return (BaseTableRepository<AbstractPersistableEntity, Serializable>)getRepository(entityEnum);
	}

	@SuppressWarnings("unchecked")
	public BaseViewRepository<AbstractImmutableEntity, Serializable> getViewRepository(IEntityEnum entityEnum) {
		Preconditions.checkArgument(entityEnum.isImmutable(), "getTableRepository() called with %s as argument when argument is mutable", entityEnum);
		return (BaseViewRepository<AbstractImmutableEntity, Serializable>)getRepository(entityEnum);
	}

	public BaseEntityRepository<? extends AbstractEntity, ? extends Serializable> getRepository(IEntityEnum entityEnum) {
		var repository = repositoryMap.get(entityEnum);
		Preconditions.checkArgument(repository != null, "Can not find a repository to handle: %s", entityEnum);
		return repository;
	}

}
