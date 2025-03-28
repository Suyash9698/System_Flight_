package com.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.auth.entity.AdminDetail;
import com.auth.entity.UserDetail;


@Repository
public interface AdminLoginRepository extends JpaRepository<AdminDetail,Integer>{
	
	@Query("SELECT u FROM AdminDetail u WHERE u.registerName = :registerName")
    List<AdminDetail> findByRegisterAdminName(@Param("registerName") String registerName);
	
	@Query("SELECT u FROM AdminDetail u WHERE u.registerEmail = :registerEmail")
    List<AdminDetail> findByRegisterEmail(@Param("registerEmail") String registerEmail);
	
	@Transactional
	@Modifying
	@Query("UPDATE AdminDetail u SET u.registerName = :name WHERE u.registerEmail = :email")
	int updateEntry(@Param("email") String email, @Param("name") String name);

	
}
