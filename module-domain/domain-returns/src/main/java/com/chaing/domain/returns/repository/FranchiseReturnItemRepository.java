package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    List<ReturnItem> findAllByReturns_ReturnCodeAndDeletedAtIsNull(String returnCode);

    List<ReturnItem> findAllByReturns_ReturnStatus(ReturnStatus status);

    List<ReturnItem> findAllByReturns_ReturnStatusNot(ReturnStatus returnStatus);

    List<ReturnItem> findAllByReturnItemIdIn(List<Long> returnItemIds);

    List<ReturnItem> findAllByReturns_ReturnIdInAndDeletedAtIsNull(List<Long> returnIds);

    List<ReturnItem> findByReturns_ReturnIdAndDeletedAtIsNull(Long returnId);
}
