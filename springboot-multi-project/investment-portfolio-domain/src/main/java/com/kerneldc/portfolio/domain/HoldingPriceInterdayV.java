package com.kerneldc.portfolio.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;

import com.kerneldc.common.domain.AbstractImmutableEntity;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;

@Entity(name = "holding_price_interday_v")
@Immutable
@Getter
public class HoldingPriceInterdayV extends AbstractImmutableEntity {
	
	@CsvBindByName(column = "position_snapshot")
	private LocalDateTime positionSnapshot;
	@CsvBindByName(column = "market_value")
	private BigDecimal marketValue;
}