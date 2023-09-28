package com.kerneldc.ipm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.springframework.data.rest.core.annotation.Description;

import com.kerneldc.common.domain.AbstractImmutableEntity;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;

@Entity(name = "holding_price_interday_v")
@Immutable
@Getter
public class HoldingPriceInterdayV extends AbstractImmutableEntity {
	
	@CsvBindByName(column = "position_snapshot")
	@Description("columnDisplayOrder=1,title=As of,format=timestamp")
	private LocalDateTime positionSnapshot;
	@CsvBindByName(column = "market_value")
	@Description("columnDisplayOrder=2,format=currency")
	private BigDecimal marketValue;
}