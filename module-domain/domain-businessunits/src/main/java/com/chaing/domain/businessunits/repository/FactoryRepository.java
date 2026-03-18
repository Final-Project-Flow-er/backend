package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.repository.interfaces.FactoryRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long>, FactoryRepositoryCustom {

    @Query(value = "SELECT * FROM factory f " +
            "WHERE f.factory_code LIKE CONCAT(:prefix, '%') " +
            "ORDER BY CAST(SUBSTR(f.factory_code, LENGTH(:prefix) + 1) AS UNSIGNED) DESC " +
            "FOR UPDATE",
            nativeQuery = true)
    List<Factory> findMaxCodeByPrefix(@Param("prefix") String prefix, Pageable pageable);

    @Query("SELECT f.factoryId, f.name FROM Factory f WHERE f.factoryId IN :ids")
    List<Object[]> findNamesByIds(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(f) > 0 FROM Factory f WHERE f.name = :name AND f.deletedAt IS NULL")
    boolean existsByNameExcludeDeleted(@Param("name") String name);
}
