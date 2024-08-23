package com.kerneldc.ipm.batch.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PojoWithOutJsonRootNameAnnotation {

	@JsonProperty("root")
	private FullName root;

}
