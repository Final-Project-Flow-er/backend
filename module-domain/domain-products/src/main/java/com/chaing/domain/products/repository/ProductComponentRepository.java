package com.chaing.domain.products.repository;

import com.chaing.domain.products.entity.ProductComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductComponentRepository extends JpaRepository<ProductComponent,Long> {
    List<ProductComponent> findByProductId(Long productId);

//    void deleteAllByProductId(Long productId);
}
