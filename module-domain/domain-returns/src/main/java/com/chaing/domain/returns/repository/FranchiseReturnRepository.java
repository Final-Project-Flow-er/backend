package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseReturnRepository extends JpaRepository<Returns, Long> {
    Optional<Returns> findByFranchiseIdAndUsernameAndReturnCode(Long franchiseId, String username, String returnCode);

    Optional<Returns> findByReturnCode(String returnCode);

    List<Returns> findAllByReturnStatus(ReturnStatus status);

    List<Returns> findAllByReturnStatusNot(ReturnStatus returnStatus);

    List<Returns> findAllByReturnCodeIn(List<@NotBlank String> returnCodes);

    // 특정 가맹점의 날짜 범위 반품 (ACCEPTED 일때)
    List<Returns> findAllByFranchiseIdAndReturnStatusAndCreatedAtBetween(
            Long franchiseId, ReturnStatus returnStatus,
            LocalDateTime start, LocalDateTime end);
}
