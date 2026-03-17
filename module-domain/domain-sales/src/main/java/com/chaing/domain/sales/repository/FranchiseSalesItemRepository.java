package com.chaing.domain.sales.repository;

import com.chaing.domain.sales.entity.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseSalesItemRepository extends JpaRepository<SalesItem, Long> {

    // Sales ID 목록으로 SalesItem 조회
    List<SalesItem> findAllBySalesSalesIdIn(List<Long> salesIds);

    // 가맹점별 모든 판매 아이템 조회 (Sales와 Join Fetch 하여 N+1 방지)
    @org.springframework.data.jpa.repository.Query("SELECT si FROM SalesItem si JOIN FETCH si.sales s WHERE s.franchiseId = :franchiseId")
    List<SalesItem> findAllBySalesFranchiseId(Long franchiseId);

    // 가맹점별 기간 내 판매 아이템 조회
    @org.springframework.data.jpa.repository.Query("SELECT si FROM SalesItem si JOIN FETCH si.sales s WHERE s.franchiseId = :franchiseId AND si.createdAt BETWEEN :start AND :end")
    List<SalesItem> findAllBySalesFranchiseIdAndCreatedAtBetween(
            @org.springframework.data.repository.query.Param("franchiseId") Long franchiseId,
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

}
