package com.kerneldc.common.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class AbstractPersistableEntity extends AbstractEntity implements Serializable {
	
	protected AbstractPersistableEntity() {
		this.logicalKeyHolder = new LogicalKeyHolder();
	}

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq_gen")
	private Long id;
	
	@Embedded
	@JsonIgnore
	private LogicalKeyHolder logicalKeyHolder;
	
	@Version
	@Column(name = "version")
	private Long version;
	// expose version here since Spring JPA rest does not
	public Long getRowVersion() {
		return version;
	}
	public void setRowVersion(Long rowVersion) {
		version = rowVersion;
	}

	@CsvBindByName
	@CsvIgnore(profiles = "csvWrite") // ignore column sourceCsvLineNumber when writing out csv file (when profile is set to csvWrite)
	@Transient
	private Long sourceCsvLineNumber;
	@Transient
	private String[] sourceCsvLine;
	
    protected abstract void setLogicalKeyHolder();
    
//	@Override
//	public int hashCode() {
//		return Objects.hash(getLogicalKeyHolder().getLogicalKey());
//	}
//	@Override
//	public boolean equals(Object entity) {
//		if (this == entity)
//			return true;
//		if (entity == null)
//			return false;
//		if (getClass() != entity.getClass())
//			return false;
//		AbstractPersistableEntity other = (AbstractPersistableEntity) entity;
//		return Objects.equals(getLogicalKeyHolder().getLogicalKey(), other.getLogicalKeyHolder().getLogicalKey());
//	}

}
