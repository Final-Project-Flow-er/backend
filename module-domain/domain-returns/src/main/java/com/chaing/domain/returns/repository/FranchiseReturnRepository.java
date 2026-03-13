package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FranchiseReturnRepository extends JpaRepository<Returns, Long> {
    Optional<Returns> findByReturnCodeAndDeletedAtIsNull(String returnCode);

    List<Returns> findAllByReturnStatus(ReturnStatus status);

    List<Returns> findAllByDeletedAtIsNull();

    // 특정 가맹점의 날짜 범위 반품 (ACCEPTED 일때)
    List<Returns> findAllByFranchiseIdAndReturnStatusAndCreatedAtBetween(
            Long franchiseId, ReturnStatus returnStatus,
            LocalDateTime start, LocalDateTime end);
    List<Returns> findAllByReturnCodeInAndDeletedAtIsNull(List<String> returnCodes);

    List<Returns> findAllByReturnCodeInAndDeletedAtIsNull(Set<String> returnCodes);

    Optional<Returns> findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(Long userId, Long franchiseId, String returnCode);

    Optional<Returns> findByFranchiseIdAndUserIdAndReturnCode(Long franchiseId, Long userId, String returnCode);

    Optional<Returns> findByReturnIdAndDeletedAtIsNull(Long returnId);

    List<Returns> findAllByFranchiseIdAndDeletedAtIsNull(Long franchiseId);

    List<Returns> findAllByReturnIdIn(List<Long> returnIds);
}
