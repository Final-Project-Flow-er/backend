package com.chaing.domain.inventories.repository.interfaces;

import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryPolicyRepositoryCustom {
    List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId);
}
