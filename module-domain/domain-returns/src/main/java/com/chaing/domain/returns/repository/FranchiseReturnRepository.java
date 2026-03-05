package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseReturnRepository extends JpaRepository<Returns, Long> {
    Optional<Returns> findByFranchiseIdAndUsernameAndReturnCode(Long franchiseId, String username, String returnCode);

    Optional<Returns> findByReturnCodeAndDeletedAtIsNull(String returnCode);

    List<Returns> findAllByReturnStatus(ReturnStatus status);

    List<Returns> findAllByReturnStatusNot(ReturnStatus returnStatus);

    List<Returns> findAllByReturnCodeIn(List<@NotBlank String> returnCodes);

    Optional<Returns> findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(Long userId, Long franchiseId, String returnCode);
}
