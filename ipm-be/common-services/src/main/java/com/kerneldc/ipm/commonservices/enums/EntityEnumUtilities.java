package com.kerneldc.ipm.commonservices.enums;

import com.google.common.base.Enums;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;

public class EntityEnumUtilities {
	
	private EntityEnumUtilities() {
	    throw new IllegalStateException("Utility class not meat to be instantiated");
	}

	public static IEntityEnum getEntityEnum(String tableName) {
		IEntityEnum tEnum;
//		if (UploadTableEnum.valueOf(tableName.toUpperCase()) != null) {
		if (Enums.getIfPresent(UploadTableEnum.class, tableName.toUpperCase()).isPresent()) {
			tEnum = UploadTableEnum.valueOf(tableName.toUpperCase());
//		} else if (InvestmentPortfolioEntityEnum.valueOf(tableName.toUpperCase()) != null) {
		} else if (Enums.getIfPresent(InvestmentPortfolioEntityEnum.class, tableName.toUpperCase()).isPresent()) {
			tEnum = InvestmentPortfolioEntityEnum.valueOf(tableName.toUpperCase());
		} else {
			throw new IllegalArgumentException(String.format("There is no corresponding entity enum for [%s]", tableName));
		}
		return tEnum;
	}

}
