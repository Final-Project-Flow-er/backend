package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Factory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {

    @Query(value = "SELECT * FROM factory f " +
            "WHERE f.factory_code LIKE CONCAT(:prefix, '%') " +
            "ORDER BY CAST(SUBSTR(f.factory_code, LENGTH(:prefix) + 1) AS UNSIGNED) DESC " +
            "FOR UPDATE",
            nativeQuery = true)
    List<Factory> findMaxCodeByPrefix(@Param("prefix") String prefix, Pageable pageable);
}
