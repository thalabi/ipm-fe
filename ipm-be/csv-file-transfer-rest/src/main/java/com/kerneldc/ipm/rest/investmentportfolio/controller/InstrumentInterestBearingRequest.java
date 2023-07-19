package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.TermEnum;

import lombok.Data;

@Data
public class InstrumentInterestBearingRequest {

    @Positive
    private Long id;
    @NotNull
    private InstrumentTypeEnum instrumentType;
    @NotNull
    @Size(max = 16)
    private String ticker;
    @NotNull
    private CurrencyEnum currency;
    @Size(max = 64)
    @NotNull
    private String name;
    @NotNull
    private InterestBearingTypeEnum interestBearingType;
    @NotNull
	private FinancialInstitutionEnum financialInstitution;
	@Positive
	private BigDecimal price;
    @Positive
	private Float interestRate;
	private TermEnum term;
	private OffsetDateTime maturityDate;
    @Positive
	private Float promotionalInterestRate;
	private OffsetDateTime promotionEndDate;
}
