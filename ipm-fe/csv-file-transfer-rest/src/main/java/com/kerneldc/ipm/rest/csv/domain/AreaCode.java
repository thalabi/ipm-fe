package com.kerneldc.ipm.rest.csv.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.CountryEnum;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "area_code_seq", allocationSize = 1)
@Getter @Setter

public class AreaCode extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String code;
	@CsvBindByName
	private String location;
	@Enumerated(EnumType.STRING)
	@CsvBindByName
	private CountryEnum country;
	
	public void setCode(String code) {
		this.code = code;
		setLogicalKeyHolder();		
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		getLogicalKeyHolder().setLogicalKey(code);
	}

}
