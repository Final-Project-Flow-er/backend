package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    Optional<Franchise> findFirstByFranchiseCodeStartingWithOrderByFranchiseCodeDesc(String prefix);
}
