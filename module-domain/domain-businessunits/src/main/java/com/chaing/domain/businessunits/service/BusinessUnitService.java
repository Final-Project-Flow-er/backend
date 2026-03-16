package com.chaing.domain.businessunits.service;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BusinessUnitService {

    BusinessUnitInternal getById(Long id);
    Map<Long, String> getNamesByIds(List<Long> ids);
    Page<BusinessUnitInternal> getBusinessUnitList(BusinessUnitSearchCondition condition, Pageable pageable);
    BusinessUnitInternal updateInfo(Long id, BusinessUnitUpdateCommand command);

}
