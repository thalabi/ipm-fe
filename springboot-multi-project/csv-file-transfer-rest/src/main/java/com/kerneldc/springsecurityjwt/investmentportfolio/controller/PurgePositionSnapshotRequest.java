package com.kerneldc.springsecurityjwt.investmentportfolio.controller;

import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PurgePositionSnapshotRequest {

    @NotNull
    private OffsetDateTime positionSnapshot;
}
