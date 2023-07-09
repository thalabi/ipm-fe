package com.kerneldc.ipm.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.NoRepositoryBean;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;

@NoRepositoryBean
public interface BaseInstrumentDetailRepository<T extends AbstractPersistableEntity, ID extends Serializable>
		extends BaseTableRepository<T, ID> {

	@EntityGraph(attributePaths = { "instrument" })
	List<IInstrumentDetail> findByInstrumentIdIn(Collection<Long> ids);
}
