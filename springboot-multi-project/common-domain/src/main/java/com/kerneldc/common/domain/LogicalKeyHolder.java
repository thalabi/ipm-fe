package com.kerneldc.common.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class LogicalKeyHolder implements Serializable, ILogicallyKeyed {

	private static final long serialVersionUID = 1L;

	@Column(name = COLUMN_LK)
	private String logicalKey;
}
