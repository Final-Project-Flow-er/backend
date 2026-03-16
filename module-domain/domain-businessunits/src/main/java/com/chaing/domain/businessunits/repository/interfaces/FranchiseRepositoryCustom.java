package com.chaing.domain.businessunits.repository.interfaces;

import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.entity.Franchise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FranchiseRepositoryCustom {

    Page<Franchise> search(BusinessUnitSearchCondition condition, Pageable pageable);
}
