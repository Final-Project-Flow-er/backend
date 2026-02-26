package com.chaing.domain.products.repository;

import com.chaing.domain.products.entity.ProductComponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentRepository extends JpaRepository<ProductComponent,Long> {
}
