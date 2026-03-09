package com.chaing.domain.sales.repository;

import com.chaing.domain.sales.entity.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseSalesItemRepository extends JpaRepository<SalesItem, Long> {

    // Sales ID 목록으로 SalesItem 조회
    List<SalesItem> findAllBySalesSalesIdIn(List<Long> salesIds);

}
