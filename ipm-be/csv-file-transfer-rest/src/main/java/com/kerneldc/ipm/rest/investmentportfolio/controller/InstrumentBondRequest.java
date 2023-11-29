package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.PaymentFrequencyEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InstrumentBondRequest {

	private Long id;
    @NotNull
	private Instrument instrument;
    @NotNull
    private String issuer;
    @NotNull
	private String cusip;
	@Positive
	private BigDecimal price;
    @Positive
	private BigDecimal coupon;
    @NotNull
	private OffsetDateTime issueDate;
    @NotNull
	private OffsetDateTime maturityDate;
    @NotNull
	private PaymentFrequencyEnum paymentFrequency;
    @NotNull
	private OffsetDateTime nextPaymentDate;
	@NotNull
	private Boolean emailNotification;
	private Long rowVersion;
}
