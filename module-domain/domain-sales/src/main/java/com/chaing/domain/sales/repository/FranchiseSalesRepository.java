package com.chaing.domain.sales.repository;

import com.chaing.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseSalesRepository extends JpaRepository<Sales, Long> {
    Optional<Sales> findByFranchiseIdAndSalesCode(Long franchiseId, String salesCode);

    // 특정 가맹점의 날짜 범위 매출 (취소 안 된 것만)
    List<Sales> findAllByFranchiseIdAndIsCanceledFalseAndCreatedAtBetween(
            Long franchiseId, LocalDateTime start, LocalDateTime end);

    List<Sales> findAllByFranchiseIdAndCreatedAtBetween(
            Long franchiseId, LocalDateTime start, LocalDateTime end);
}
