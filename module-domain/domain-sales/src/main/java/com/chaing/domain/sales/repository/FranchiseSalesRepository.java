package com.chaing.domain.sales.repository;

import com.chaing.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranchiseSalesRepository extends JpaRepository<Sales, Long> {
    Optional<Sales> findByFranchiseIdAndSalesCode(Long franchiseId, String salesCode);
}
