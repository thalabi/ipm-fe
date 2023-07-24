package com.kerneldc.ipm.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.NoRepositoryBean;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;

@NoRepositoryBean
public interface BaseInstrumentDetailRepository<T extends AbstractPersistableEntity, ID extends Serializable>
		extends BaseTableRepository<T, ID> {

	@Override
	@EntityGraph(attributePaths = {"instrument"})
	Page<T> findAll(Pageable pageable);
	
	@EntityGraph(attributePaths = { "instrument" })
	List<IInstrumentDetail> findByInstrumentIdIn(Collection<Long> ids);
}
