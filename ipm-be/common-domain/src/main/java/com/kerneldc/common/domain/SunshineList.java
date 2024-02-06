package com.kerneldc.common.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.rest.core.annotation.Description;

import com.opencsv.bean.CsvBindByName;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "sunshine_list_seq", allocationSize = 1)
@Getter @Setter

public class SunshineList extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	public static final int SALARY_INDEX = 3;
	public static final int BENEFITS_INDEX = 4;

	public static final String LAST_NAME = "Last Name";
	public static final String ALTERNATE_LAST_NAME = "Surname";
	public static final String SALARY = "Salary Paid";
	public static final String ALTERNATE_SALARY = "Salary";
	public static final String BENEFITS = "Taxable Benefits";
	public static final String ALTERNATE_BENEFITS = "Benefits";
	public static final String JOB_TITLE = "Job Title";
	public static final String ALTERNATE_JOB_TITLE = "Position";
	public static final String CALENDAR_YEAR = "Calendar Year";
	public static final String ALTERNATE_CALENDAR_YEAR = "Year";

	@CsvBindByName
	@Description("columnDisplayOrder=8")
	private String sector;
	
	@CsvBindByName(column = LAST_NAME)
	@Setter(AccessLevel.NONE)
	@Description("columnDisplayOrder=3,filterable=true,type=text")
	private String lastName;
	
	@CsvBindByName(column = "First Name")
	@Setter(AccessLevel.NONE)
	@Description("columnDisplayOrder=2,filterable=true,type=text")
	private String firstName;
	
	@CsvBindByName(column = SALARY)
	@Description("columnDisplayOrder=4,format=currency,filterable=true,type=numeric,fractionDigits=2")
	private BigDecimal salary;
	
	@CsvBindByName(column = BENEFITS)
	@Description("columnDisplayOrder=5,format=currency,filterable=true,type=numeric,fractionDigits=2")
	private BigDecimal benefits;
	
	@CsvBindByName
	@Description("columnDisplayOrder=6")
	private String employer;
	
	@CsvBindByName(column = JOB_TITLE)
	@Description("columnDisplayOrder=7")
	private String jobTitle;
	
	@CsvBindByName(column = CALENDAR_YEAR)
	@Description("columnDisplayOrder=1,filterable=true,type=numeric,sortOrder=1,sortDirection=desc")
	private Short calendarYear;
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
		setLogicalKeyHolder();
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(lastName, firstName, UUID.randomUUID().toString());
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
