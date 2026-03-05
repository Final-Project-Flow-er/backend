package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.ReturnItemBoxCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseReturnItemBoxCodeRepository extends JpaRepository<ReturnItemBoxCode, Long> {
    List<ReturnItemBoxCode> findAllByReturnItem_ReturnItemIdIn(List<Long> returnItemIds);
}
