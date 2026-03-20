package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FranchiseReturnRepository extends JpaRepository<Returns, Long>, FranchiseReturnRepositoryCustom {
    Optional<Returns> findByReturnCodeAndDeletedAtIsNull(String returnCode);

    List<Returns> findAllByReturnStatus(ReturnStatus status);

    List<Returns> findAllByDeletedAtIsNull();

    // 특정 가맹점의 날짜 범위 반품 (여러 상태 지원)
    List<Returns> findAllByFranchiseIdAndReturnStatusInAndCreatedAtBetween(
            Long franchiseId, List<ReturnStatus> statuses,
            LocalDateTime start, LocalDateTime end);

    // 특정 가맹점의 날짜 범위 반품 (ACCEPTED 일때) - 기존 메서드 유지
    List<Returns> findAllByFranchiseIdAndReturnStatusAndCreatedAtBetween(
            Long franchiseId, ReturnStatus returnStatus,
            LocalDateTime start, LocalDateTime end);

    List<Returns> findAllByReturnCodeInAndDeletedAtIsNull(List<String> returnCodes);

    List<Returns> findAllByReturnCodeInAndDeletedAtIsNull(Set<String> returnCodes);

    Optional<Returns> findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(Long userId, Long franchiseId,
            String returnCode);

    Optional<Returns> findByFranchiseIdAndUserIdAndReturnCode(Long franchiseId, Long userId, String returnCode);

    Optional<Returns> findByReturnIdAndDeletedAtIsNull(Long returnId);

    List<Returns> findAllByFranchiseIdAndDeletedAtIsNull(Long franchiseId);

    List<Returns> findAllByReturnIdInAndDeletedAtIsNull(List<Long> returnIds);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Returns r SET r.returnStatus = :returnStatus WHERE r.returnCode IN :returnCodes")
    void updateReturnStatusByReturnCodeIn(@Param("returnCodes") List<String> returnCodes,
            @Param("returnStatus") ReturnStatus returnStatus);
}
