package com.kerneldc.ipm.commonservices.repository;

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
import com.kerneldc.ipm.repository.BaseInstrumentDetailRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EntityRepositoryFactory {

	private Map<IEntityEnum, BaseEntityRepository<? extends AbstractEntity, ? extends Serializable>> baseEntityRepositoryMap = new HashMap<>();
	private Map<IEntityEnum, BaseInstrumentDetailRepository<? extends AbstractEntity, ? extends Serializable>> instrumentDetailRepositoryMap = new HashMap<>();

	public EntityRepositoryFactory(
			Collection<BaseEntityRepository<? extends AbstractEntity, ? extends Serializable>> baseEntityRepositories,
			Collection<BaseInstrumentDetailRepository<? extends AbstractEntity, ? extends Serializable>> baseInstrumentDetailRepositories) {
		
		LOGGER.debug("Loading baseEntityRepositoryMap with:");
		baseEntityRepositoryMap = baseEntityRepositories.stream().collect(Collectors.toMap(BaseEntityRepository::canHandle, r -> r));
		baseEntityRepositoryMap.forEach((entityEnum, repository) -> LOGGER.debug("[{}, {}, {}]", entityEnum,
				entityEnum.getEntity().getSimpleName(),
				entityEnum.isImmutable() ? "immutable entity" : "mutable entity"));
		LOGGER.debug("Loading instrumentDetailRepositoryMap with:");
		instrumentDetailRepositoryMap = baseInstrumentDetailRepositories.stream().collect(Collectors.toMap(BaseInstrumentDetailRepository::canHandle, r -> r));
		instrumentDetailRepositoryMap.forEach((entityEnum, repository) -> LOGGER.debug("[{}, {}, {}]", entityEnum,
				entityEnum.getEntity().getSimpleName(),
				entityEnum.isImmutable() ? "immutable entity" : "mutable entity"));

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
		var repository = baseEntityRepositoryMap.get(entityEnum);
		Preconditions.checkArgument(repository != null, "Can not find a repository to handle: %s", entityEnum);
		return repository;
	}

	public BaseInstrumentDetailRepository<? extends AbstractEntity, ? extends Serializable> getBaseInstrumentRepository(IEntityEnum entityEnum) {
		var repository = instrumentDetailRepositoryMap.get(entityEnum);
		Preconditions.checkArgument(repository != null, "Can not find a repository to handle: %s", entityEnum);
		return repository;
	}
}
