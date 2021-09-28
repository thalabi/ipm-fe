package com.kerneldc.springsecurityjwt.investmentportfolio.controller;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class SaveHoldingRequest {

    @Positive
    private Long id;
    @Positive
    private Long portfolioId;
    @Positive
    private Long instrumentId;
    @Positive
    private Float quantity;
    @NotNull
    private LocalDate asOfDate;
    @PositiveOrZero
    private Long version;
}
