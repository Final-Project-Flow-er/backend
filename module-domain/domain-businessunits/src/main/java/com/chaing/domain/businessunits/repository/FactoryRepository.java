package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {

    Optional<Factory> findFirstByFactoryCodeStartingWithOrderByFactoryCodeDesc(String prefix);
}
