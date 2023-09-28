package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class SaveHoldingRequest {

    @Positive
    private Long id;
    @Positive
    private Long portfolioId;
    @Min(-2) // -1 is CAD Cash, -2 is USD Cash
    private Long instrumentId;
    @Positive
    private BigDecimal quantity;
    @NotNull
    private OffsetDateTime asOfDate;
    @PositiveOrZero
    private Long version;
}
