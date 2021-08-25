package com.kerneldc.common;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;

import com.kerneldc.common.domain.AbstractImmutableEntity;

@NoRepositoryBean
public interface BaseViewRepository<T extends AbstractImmutableEntity, ID extends Serializable> extends BaseEntityRepository<T, ID> {	

}
