package com.kerneldc.portfolio.domain;

import com.google.common.base.Enums;
import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;

public enum InvestmentPortfolioTableEnum implements IEntityEnum {
	INSTRUMENT(Instrument.class, false, new String[] {"TICKER","EXCHANGE","CURRENCY"}),
	PORTFOLIO(Portfolio.class, false, new String[] {"INSTITUTION","ACCOUNT_NUMBER","CURRENCY"}),
	HOLDING(Holding.class, false, new String[] {"AS_OF_DATE","TICKER","EXCHANGE","QUANTITY","INSTITUTION","ACCOUNT_NUMBER"}),
	POSITION(Position.class, false),
	EXCHANGE_RATE(ExchangeRate.class, false, new String[] {"DATE","FROM_CURRENCY","TO_CURRENCY", "RATE"}),
	HOLDING_PRICE_INTERDAY_V(HoldingPriceInterdayV.class, true, new String[] {"POSITION_SNAPSHOT","MARKET_VALUE"})
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
