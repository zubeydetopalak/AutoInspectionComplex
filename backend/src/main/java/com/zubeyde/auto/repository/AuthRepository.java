package com.zubeyde.auto.repository;

import com.zubeyde.auto.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface AuthRepository extends JpaRepository<Admin, Long> {
    @Query("SELECT a FROM Admin a WHERE a.username = ?1 AND a.password = ?2")
     Admin findByUsernameandPassword(String username, String password);
}
