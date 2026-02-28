package com.chaing.domain.users.repository;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    @Query("SELECT MAX(u.employeeNumber) FROM User u WHERE u.role = :role")
    Optional<String> findMaxEmployeeNumberByRole(@Param("role") UserRole role);

    @Query("SELECT MAX(u.loginId) FROM User u WHERE u.role = :role")
    Optional<String> findMaxLoginIdByRole(@Param("role") UserRole role);

    boolean existsByEmail(String email);
}
