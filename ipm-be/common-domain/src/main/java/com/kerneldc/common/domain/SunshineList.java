package com.kerneldc.common.domain;

import java.math.BigDecimal;
import java.util.UUID;

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
	public static final String YEAR = "Calendar Year";
	public static final String ALTERNATE_YEAR = "Year";

	//@CsvBindByPosition(position = 0)
	@CsvBindByName
	private String sector;
	
	//@CsvBindByPosition(position = 1)
	@CsvBindByName(column = LAST_NAME)
	@Setter(AccessLevel.NONE)
	private String lastName;
	
	//@CsvBindByPosition(position = 2)
	@CsvBindByName(column = "First Name")
	@Setter(AccessLevel.NONE)
	private String firstName;
	
	//@CsvBindByPosition(position = 3)
	@CsvBindByName(column = SALARY)
	private BigDecimal salary;
	
	//@CsvBindByPosition(position = 4)
	@CsvBindByName(column = BENEFITS)
	private BigDecimal benefits;
	
	//@CsvBindByPosition(position = 5)
	@CsvBindByName
	private String employer;
	
	//@CsvBindByPosition(position = 6)
	@CsvBindByName(column = JOB_TITLE)
	private String jobTitle;
	
	//@CsvBindByPosition(position = 7)
	@CsvBindByName(column = YEAR)
	private short year;
	
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
