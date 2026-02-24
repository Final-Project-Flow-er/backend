package com.chaing.domain.sales.repository;

import com.chaing.domain.sales.entity.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FranchiseSalesItemRepository extends JpaRepository<SalesItem, Long> {
}
