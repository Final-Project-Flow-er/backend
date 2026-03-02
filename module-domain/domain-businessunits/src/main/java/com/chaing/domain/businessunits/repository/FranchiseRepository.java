package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Franchise;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Franchise f WHERE f.franchiseCode LIKE :prefix% " +
            "ORDER BY CAST(SUBSTRING(f.franchiseCode, LENGTH(:prefix) + 1) AS int) DESC")
    Optional<Franchise> findFirstByFranchiseCodeStartingWithOrderByFranchiseCodeDesc(String prefix);
}
