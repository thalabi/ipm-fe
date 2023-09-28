package com.kerneldc.ipm.domain;



import jakarta.persistence.Entity;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

// This table should have one record only.
@Entity
@Getter @Setter
public class FixedIncomeAudit extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private Boolean change;

	public void setChange(Boolean change) {
		this.change = change;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(change);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
