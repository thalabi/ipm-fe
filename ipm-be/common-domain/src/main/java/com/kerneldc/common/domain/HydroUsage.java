package com.kerneldc.common.domain;

import org.springframework.data.rest.core.annotation.Description;

import com.kerneldc.common.enums.ShortMonthEnum;
import com.opencsv.bean.CsvBindByName;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "hydro_usage_seq", allocationSize = 1)
@Getter @Setter

public class HydroUsage extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	@Description("columnDisplayOrder=1")
	private Integer year;
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	@Description("columnDisplayOrder=2")
	private ShortMonthEnum month;
	@CsvBindByName
	@Description("columnDisplayOrder=7")
	private Integer highTemp;
	@CsvBindByName
	@Description("columnDisplayOrder=6")
	private Integer lowTemp;
	@CsvBindByName(column = "Off-Peak")
	@Description("columnDisplayOrder=3,title=Off Peak")
	private Float offPeak;
	@CsvBindByName(column = "Mid-Peak")
	@Description("columnDisplayOrder=4,title=Mid Peak")
	private Float midPeak;
	@CsvBindByName(column = "On-Peak")
	@Description("columnDisplayOrder=5,title=On Peak")
	private Float onPeak;

	public void setYear(Integer year) {
		this.year = year;
		setLogicalKeyHolder();
	}
	public void setMonth(ShortMonthEnum month) {
		this.month = month;
		setLogicalKeyHolder();
	}
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(year, ShortMonthEnum.getNumericValue(month));
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
