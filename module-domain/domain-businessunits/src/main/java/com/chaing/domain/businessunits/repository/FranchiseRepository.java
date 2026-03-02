package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Franchise;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Franchise> findFirstByFranchiseCodeStartingWithOrderByFranchiseCodeDesc(String prefix);
}
