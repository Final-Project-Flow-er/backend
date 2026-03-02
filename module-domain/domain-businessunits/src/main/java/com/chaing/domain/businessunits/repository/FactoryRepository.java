package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Factory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Factory> findFirstByFactoryCodeStartingWithOrderByFactoryCodeDesc(String prefix);
}
