package com.kerneldc.springsecurityjwt.security.domain.user;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.converter.HashingConverter;
import com.kerneldc.common.domain.converter.HmacSHA256KeyConverter;
import com.kerneldc.springsecurityjwt.security.domain.group.Group;
import com.kerneldc.springsecurityjwt.security.domain.permission.Permission;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "user_seq", allocationSize = 1)
@NamedEntityGraph(
	name = "userGroupSetGraph",
	attributeNodes = @NamedAttributeNode(value = "groupSet"))
@NamedEntityGraph(
	name = "userGroupSetPermissionSetGraph",
	attributeNodes = @NamedAttributeNode(value = "groupSet", subgraph = "permissions"), 
		subgraphs = @NamedSubgraph(name = "permissions", attributeNodes = @NamedAttributeNode("permissionSet")))
@Getter @Setter
public class User extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_GROUP_SET = "groupSet";

	public static final Function<User, Object> idExtractor = User::getId;

	@Column(unique = true)
	@Setter(AccessLevel.NONE)
    private String username;
	@Column
	@Convert(converter = HashingConverter.class)
	@JsonIgnore
    private String password;
    private Boolean enabled;
    private String firstName; 
    private String lastName;
    private String email;
    private String cellPhone;
    @Convert(converter = HmacSHA256KeyConverter.class)
    private Key resetPasswordJwtKey;

    @ManyToMany
    @JoinTable(name = "user_group", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn( name="group_id")) 
    private Set<Group> groupSet;// = new HashSet<>(); 

    @Transient 
    private Set<Permission> permissionSet; 

	private LocalDateTime created = LocalDateTime.now();
	private LocalDateTime modified = LocalDateTime.now();
	
	public void setUsername(String username) {
		this.username = username;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		getLogicalKeyHolder().setLogicalKey(username);
	}

}
