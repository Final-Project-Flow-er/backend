package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.Returns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranchiseReturnRepository extends JpaRepository<Returns, Long> {
    Optional<Returns> findByFranchiseIdAndUsernameAndReturnCode(Long franchiseId, String username, String returnCode);
}
