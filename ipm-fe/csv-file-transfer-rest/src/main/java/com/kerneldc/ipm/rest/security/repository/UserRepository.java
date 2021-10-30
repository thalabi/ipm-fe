package com.kerneldc.ipm.rest.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kerneldc.ipm.rest.security.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	List<User> findAllByOrderByUsername();

	@EntityGraph("userGroupSetPermissionSetGraph")
	List<User> findByUsernameAndEnabled(String username, Boolean enabled);

	@EntityGraph("userGroupSetGraph")
	List<User> findByUsername(String username);

//	@EntityGraph("userGroupSetPermissionSetGraph")
//	Page<User> findAll(@Nullable Specification<User> spec, Pageable pageable);

	List<User> findByEmail(String email);
}
