package com.chaing.domain.businessunits.repository.interfaces;

import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.entity.Factory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FactoryRepositoryCustom {

    Page<Factory> search(BusinessUnitSearchCondition condition, Pageable pageable);
}
