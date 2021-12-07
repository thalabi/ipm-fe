package com.kerneldc.ipm.domain;

import com.google.common.base.Enums;
import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;

public enum InvestmentPortfolioTableEnum implements IEntityEnum {
	INSTRUMENT(Instrument.class, false, new String[] {"NAME","TICKER","EXCHANGE","CURRENCY"}),
	PORTFOLIO(Portfolio.class, false, new String[] {"INSTITUTION","ACCOUNT_NUMBER","CURRENCY","NAME"}),
	HOLDING(Holding.class, false, new String[] {"AS_OF_DATE","TICKER","EXCHANGE","QUANTITY","INSTITUTION","ACCOUNT_NUMBER"}),
	POSITION(Position.class, false, new String[] {"TICKER", "EXCHANGE", "ACCOUNT_NUMBER", "INSTITUTION", "PRICE_TIMESTAMP", "POSITION_TIMESTAMP", "QUANTITY"}),
	EXCHANGE_RATE(ExchangeRate.class, false, new String[] {"AS_OF_DATE","FROM_CURRENCY","TO_CURRENCY", "RATE"}),
	HOLDING_PRICE_INTERDAY_V(HoldingPriceInterdayV.class, true, new String[] {"POSITION_SNAPSHOT","MARKET_VALUE"}),
	PRICE(Price.class, false, new String[] {"TICKER", "EXCHANGE", "PRICE", "PRICE_TIMESTAMP", "PRICE_TIMESTAMP_FROM_SOURCE"})
	;

	Class<? extends AbstractEntity> entity;
	boolean immutable;
	String[] writeColumnOrder;
	
	InvestmentPortfolioTableEnum(Class<? extends AbstractEntity> entity, boolean immutable) {
		this.entity = entity;
		this.immutable = immutable;
	}
	InvestmentPortfolioTableEnum(Class<? extends AbstractEntity> entity, boolean immutable, String[] writeColumnOrder) {
		this.entity = entity;
		this.immutable = immutable;
		this.writeColumnOrder = writeColumnOrder;
	}
	
	@Override
	public Class<? extends AbstractEntity> getEntity() {
		return entity;
	}
	
	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public String[] getWriteColumnOrder() {
		return writeColumnOrder;
	}
	
	public static IEntityEnum valueIfPresent(String name) {
	    return Enums.getIfPresent(InvestmentPortfolioTableEnum.class, name).orNull();
	}

}
