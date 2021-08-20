package com.kerneldc.springsecurityjwt.security.domain.permission;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.springsecurityjwt.security.domain.group.Group;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "permission_seq", allocationSize = 1)
@Getter @Setter
public class Permission extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	@Column(unique = true)
	@Setter(AccessLevel.NONE)
    private String name;
    private String description; 
    
//    @ManyToMany(mappedBy = "permissionSet")
    @Transient
    private Set<Group> groupSet = new HashSet<>(); 


	private LocalDateTime created = LocalDateTime.now();
	private LocalDateTime modified = LocalDateTime.now();
	

	public void setName(String name) {
		this.name = name;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		getLogicalKeyHolder().setLogicalKey(name);
	}

}
