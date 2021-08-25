package com.kerneldc.springsecurityjwt.csv.controller;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableListResponse {

	private List<String> tableList;
}