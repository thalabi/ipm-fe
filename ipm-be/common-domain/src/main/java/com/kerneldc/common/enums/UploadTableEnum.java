package com.kerneldc.common.enums;

import java.util.Arrays;

import com.google.common.base.Enums;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.AreaCode;
import com.kerneldc.common.domain.BioStats;
import com.kerneldc.common.domain.HydroUsage;
import com.kerneldc.common.domain.Sales;

public enum UploadTableEnum implements IEntityEnum {
	SALES(Sales.class),
	BIO_STATS(BioStats.class),
	AREA_CODE(AreaCode.class, new String[] {"CODE","LOCATION","COUNTRY"}),
	HYDRO_USAGE(HydroUsage.class, new String[] {"YEAR","MONTH","MID-PEAK","OFF-PEAK","ON-PEAK","HIGHTEMP","LOWTEMP"}),
	//INSTRUMENT(Instrument.class, new String[] {"TICKER","EXCHANGE","CURRENCY"}),
	//PORTFOLIO(Portfolio.class, new String[] {"INSTITUTION","ACCOUNT_NUMBER","CURRENCY"}),
	//HOLDING(Holding.class, new String[] {"AS_OF_DATE","TICKER","EXCHANGE","QUANTITY","INSTITUTION","ACCOUNT_NUMBER"})
	;
	
	Class<? extends AbstractPersistableEntity> entity;
	String[] writeColumnOrder;
	
	UploadTableEnum(Class<? extends AbstractPersistableEntity> entity) {
		this.entity = entity;
	}
	UploadTableEnum(Class<? extends AbstractPersistableEntity> entity, String[] writeColumnOrder) {
		this.entity = entity;
		// tag SOURCECSVLINENUMBER to the end of the writeColumnOrder
		this.writeColumnOrder = Arrays.copyOf(writeColumnOrder, writeColumnOrder.length+1);
		this.writeColumnOrder[this.writeColumnOrder.length-1] = "SOURCECSVLINENUMBER";  
	}
	
	@Override
	public Class<? extends AbstractPersistableEntity> getEntity() {
		return entity;
	}

	@Override
	public boolean isImmutable() {
		return false;
	}
	
	@Override
	public String[] getWriteColumnOrder() {
		return writeColumnOrder;
	}
	
//	@Override
	public static IEntityEnum valueIfPresent(String name) {
	    return Enums.getIfPresent(UploadTableEnum.class, name).orNull();
	}
}
