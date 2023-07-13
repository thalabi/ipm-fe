package com.kerneldc.ipm.domain;

import java.util.Arrays;

import com.google.common.base.Enums;
import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentEtf;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMoneyMarket;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMutualFund;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;

public enum InvestmentPortfolioTableEnum implements IEntityEnum {
	INSTRUMENT(Instrument.class, false, new String[] {"NAME","TICKER","EXCHANGE","CURRENCY"}),
	INSTRUMENT_STOCK(InstrumentStock.class, false, new String[] {}),
	INSTRUMENT_ETF(InstrumentEtf.class, false, new String[] {}),
	INSTRUMENT_MUTUAL_FUND(InstrumentMutualFund.class, false, new String[] {}),
	INSTRUMENT_MONEY_MARKET(InstrumentMoneyMarket.class, false, new String[] {}),
	PORTFOLIO(Portfolio.class, false, new String[] {"INSTITUTION","ACCOUNT_NUMBER","CURRENCY","NAME"}),
	HOLDING(Holding.class, false, new String[] {"AS_OF_DATE","TICKER","EXCHANGE","QUANTITY","INSTITUTION","ACCOUNT_NUMBER"}),
	POSITION(Position.class, false, new String[] {"TICKER", "EXCHANGE", "ACCOUNT_NUMBER", "INSTITUTION", "PRICE_TIMESTAMP", "POSITION_TIMESTAMP", "QUANTITY"}),
	EXCHANGE_RATE(ExchangeRate.class, false, new String[] {"AS_OF_DATE","FROM_CURRENCY","TO_CURRENCY", "RATE"}),
	HOLDING_PRICE_INTERDAY_V(HoldingPriceInterdayV.class, true, new String[] {"POSITION_SNAPSHOT","MARKET_VALUE"}),
	PRICE(Price.class, false, new String[] {"TICKER", "EXCHANGE", "PRICE", "PRICE_TIMESTAMP", "PRICE_TIMESTAMP_FROM_SOURCE"/*, "SOURCECSVLINENUMBER"*/}),
	INSTRUMENT_BY_ACCOUNT_V(InstrumentByAccountV.class, true, new String[] {"TICKER_EXCHANGE", "INSTRUMENT_NAME", "QUANTITY", "ACCOUNT_NUMBER", "ACCOUNT_NAME"})
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
		// tag SOURCECSVLINENUMBER to the end of the writeColumnOrder
		this.writeColumnOrder = Arrays.copyOf(writeColumnOrder, writeColumnOrder.length+1);
		this.writeColumnOrder[this.writeColumnOrder.length-1] = "SOURCECSVLINENUMBER";  
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
