package com.kerneldc.common.domain;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class AbstractImmutableEntity extends AbstractEntity {
	
	@Id
	private Long id;

}
