package com.chaing.domain.inventories.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record InventoryAlertResponse(
        List<SafetyStockAlertResponse> safetyStockAlerts,
        List<ExpirationAlertResponse> expirationAlerts
) {}