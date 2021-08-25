package com.kerneldc.common.enums;

import com.kerneldc.common.domain.AbstractEntity;

public interface IEntityEnum {

	Class<? extends AbstractEntity> getEntity();
	boolean isImmutable();
	String[] getWriteColumnOrder();
}