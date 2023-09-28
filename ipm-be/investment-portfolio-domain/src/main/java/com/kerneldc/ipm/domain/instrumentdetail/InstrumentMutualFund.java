package com.kerneldc.ipm.domain.instrumentdetail;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "inst_mutual_fund")
@SequenceGenerator(name = "default_seq_gen", sequenceName = "inst_mutual_fund_seq", allocationSize = 1)
@Getter @Setter
public class InstrumentMutualFund extends AbstractInstrumentDetailEntity implements IInstrumentDetail {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String company;


	public void setCompany(String company) {
		this.company = company;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(instrument.getTicker(), company);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
