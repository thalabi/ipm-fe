package com.kerneldc.ipm.domain;

public enum InstrumentTypeEnum {
	CASH(null),
	STOCK(InvestmentPortfolioEntityEnum.INSTRUMENT_STOCK),
	ETF(InvestmentPortfolioEntityEnum.INSTRUMENT_ETF),
	MUTUAL_FUND(InvestmentPortfolioEntityEnum.INSTRUMENT_MUTUAL_FUND),
	INTEREST_BEARING(InvestmentPortfolioEntityEnum.INSTRUMENT_INTEREST_BEARING),
	BOND(InvestmentPortfolioEntityEnum.INSTRUMENT_BOND);//, MONEY_MARKET, INV_SAVINGS_ACC, GIC, SAVINGS_ACC
	
	InvestmentPortfolioEntityEnum investmentPortfolioTableEnum;
	InstrumentTypeEnum(InvestmentPortfolioEntityEnum investmentPortfolioTableEnum) {
		this.investmentPortfolioTableEnum = investmentPortfolioTableEnum;
	}
	
	public InvestmentPortfolioEntityEnum getInvestmentPortfolioTableEnum() {
		return investmentPortfolioTableEnum;
	}
}
