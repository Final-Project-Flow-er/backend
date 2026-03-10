package com.chaing.domain.products.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ProductRequest(
                String productCode,
                String name,
                String description,
                String imageUrl,
                BigDecimal price,
                BigDecimal costPrice,
                BigDecimal supplyPrice,
                Integer safetyStock,
                String status,
                Integer kcal,
                Integer weight,
                LocalDate startDate,
                LocalDate endDate,
                List<String> components) {
}
