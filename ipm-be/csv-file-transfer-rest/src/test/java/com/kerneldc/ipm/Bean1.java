package com.kerneldc.ipm;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class Bean1 {
	@CsvBindByName
	private String firstName;
}