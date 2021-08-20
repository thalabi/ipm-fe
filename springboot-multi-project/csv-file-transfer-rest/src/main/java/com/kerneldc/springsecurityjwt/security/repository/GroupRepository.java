package com.kerneldc.springsecurityjwt.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kerneldc.springsecurityjwt.security.domain.group.Group;

public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
	List<Group> findAllByOrderByName();
}
