package com.kerneldc.ipm.rest.investmentportfolio.controller;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PurgePositionSnapshotRequest {

    @NotNull
    private OffsetDateTime positionSnapshot;
}
