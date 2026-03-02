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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Factory f WHERE f.factoryCode LIKE :prefix% " +
            "ORDER BY CAST(SUBSTRING(f.factoryCode, LENGTH(:prefix) + 1) AS int) DESC")
    List<Factory> findMaxCodeByPrefix(@Param("prefix") String prefix, Pageable pageable);
}
