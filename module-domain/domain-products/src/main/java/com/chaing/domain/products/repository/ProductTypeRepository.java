package com.chaing.domain.products.repository;

import com.chaing.domain.products.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    Optional<ProductType> findByProductType(String productType);

    boolean existsByProductType(String type);
}
