package com.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.auth.entity.AdminAllDetail;


@Repository
public interface AdminAllDetailRepository extends JpaRepository<AdminAllDetail,Integer>{
	
	@Query("SELECT u FROM AdminAllDetail u WHERE u.registerEmail = :email")
    List<AdminAllDetail> findAllAdminDetailsByEmail(@Param("email") String email);
	
	@Query("SELECT u.registerEmail FROM AdminAllDetail u")
	List<String> allAdminsEmail();
	
	@Query("SELECT u.dob FROM AdminAllDetail u WHERE u.registerEmail = :email")
	String getDob(@Param("email") String email);

	
	@Transactional
	@Modifying
	@Query("UPDATE AdminAllDetail u SET u.registerName = :name, u.dob = :dob WHERE u.registerEmail = :email")
	int updateEntry(@Param("email") String email, @Param("name") String name, @Param("dob") String dob);

}
