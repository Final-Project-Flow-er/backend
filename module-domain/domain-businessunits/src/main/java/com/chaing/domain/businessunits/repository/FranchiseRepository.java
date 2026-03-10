package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Franchise;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    @Query(value = "SELECT * FROM franchise f " +
            "WHERE f.franchise_code LIKE CONCAT(:prefix, '%') " +
            "ORDER BY CAST(SUBSTR(f.franchise_code, LENGTH(:prefix) + 1) AS UNSIGNED) DESC " +
            "FOR UPDATE",
            nativeQuery = true)
    List<Franchise> findMaxCodeByPrefix(@Param("prefix") String prefix, Pageable pageable);

    @Query("SELECT f.franchiseId, f.name FROM Franchise f WHERE f.franchiseId IN :ids")
    List<Object[]> findNamesByIds(@Param("ids") List<Long> ids);
}
