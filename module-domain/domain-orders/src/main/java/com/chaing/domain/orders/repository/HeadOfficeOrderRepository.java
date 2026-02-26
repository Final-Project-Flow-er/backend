package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.HeadOfficeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadOfficeOrderRepository extends JpaRepository<HeadOfficeOrder, Long> {
    List<HeadOfficeOrder> findAllByHqIdAndUsername(Long hqId, String username);
}
