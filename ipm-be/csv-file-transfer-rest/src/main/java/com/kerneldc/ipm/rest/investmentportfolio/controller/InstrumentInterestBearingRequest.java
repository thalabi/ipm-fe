package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.TermEnum;

import lombok.Data;

@Data
public class InstrumentInterestBearingRequest {

    @NotNull
	private Instrument instrument;
    @NotNull
    private InterestBearingTypeEnum type;
    @NotNull
	private FinancialInstitutionEnum financialInstitution;
	@Positive
	private BigDecimal price;
    @Positive
	private Float interestRate;
	private TermEnum term;
	private OffsetDateTime maturityDate;
	private Float promotionalInterestRate;
	private OffsetDateTime promotionEndDate;
	@NotNull
	private Boolean emailNotification;
}
