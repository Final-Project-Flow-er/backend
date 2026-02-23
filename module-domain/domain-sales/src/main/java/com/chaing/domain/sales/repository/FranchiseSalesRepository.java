package com.chaing.domain.sales.repository;

import com.chaing.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FranchiseSalesRepository extends JpaRepository<Sales, Long> {
    Sales findByFranchiseIdAndSalesCode(Long franchiseId, String salesCode);
}
