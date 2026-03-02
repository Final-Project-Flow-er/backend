package com.chaing.domain.products.repository;

import com.chaing.domain.products.entity.Product;
import com.chaing.domain.products.repository.interfaces.ProductRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, ProductRepositoryCustom {
    List<Product> findAllByProductIdIn(List<Long> productIds);
}
