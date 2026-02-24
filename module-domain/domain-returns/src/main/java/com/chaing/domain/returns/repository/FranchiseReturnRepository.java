package com.chaing.domain.returns.repository;

import com.chaing.domain.returns.entity.Returns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FranchiseReturnRepository extends JpaRepository<Returns, Long> {
}
