package com.kerneldc.ipm.batch.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FullName {

	@JsonProperty("firstName")
	private String firstName;
	@JsonProperty("lastName")
	private String lastName;

}
