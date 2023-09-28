package com.kerneldc.common.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "sales_seq", allocationSize = 1)
@Getter @Setter

public class Sales extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	@CsvBindByName(column = "Transaction_date")
	@CsvDate("M/d/y H:m")
	@Setter(AccessLevel.NONE)
	private LocalDateTime transactionDate;
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String product;
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private Double price;
	@CsvBindByName(column = "Payment_Type")
	@Setter(AccessLevel.NONE)
	private String paymentType;
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String name;
	@CsvBindByName
	private String city;
	@CsvBindByName
	private String state;
	@CsvBindByName
	private String country;
	@CsvBindByName(column = "Account_Created")
	@CsvDate("M/d/y H:m")
	private LocalDateTime accountCreated;
	@CsvBindByName(column = "Last_Login")
	@CsvDate("M/d/y H:m")
	private LocalDateTime lastLogin;
	@CsvBindByName
	private Float latitude;
	@CsvBindByName
	private Float longitude;	
	@CsvBindByName(column = "us zip")
	private String usZip;
	
	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
		setLogicalKeyHolder();
	}

	public void setProduct(String product) {
		this.product = product;
		setLogicalKeyHolder();
	}

	public void setPrice(Double price) {
		this.price = price;
		setLogicalKeyHolder();
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
		setLogicalKeyHolder();
	}

	public void setName(String name) {
		this.name = name;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(Objects.toString(transactionDate, StringUtils.EMPTY), product, price, paymentType, name);
		setLogicalKeyHolder(logicalKeyHolder);
	}

}
