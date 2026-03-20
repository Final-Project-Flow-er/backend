package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Headquarter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadquarterRepository extends JpaRepository<Headquarter, Long> {

    @Query("SELECT f.hqId, f.name FROM Headquarter f WHERE f.hqId IN :ids")
    List<Object[]> findNamesByIds(@Param("ids") List<Long> ids);

    Page<Headquarter> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT f.hqId FROM Headquarter f WHERE f.name LIKE %:name% AND f.deletedAt IS NULL")
    List<Long> findAllIdsByName(@Param("name") String name);
}
