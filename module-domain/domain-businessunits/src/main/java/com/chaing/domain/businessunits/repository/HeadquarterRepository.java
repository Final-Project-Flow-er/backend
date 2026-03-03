package com.chaing.domain.businessunits.repository;

import com.chaing.domain.businessunits.entity.Headquarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadquarterRepository extends JpaRepository<Headquarter,Long> {
}
