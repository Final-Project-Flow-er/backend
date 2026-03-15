package com.chaing.domain.businessunits.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessUnitManagementService extends BusinessUnitService {

    BusinessUnitInternal create(BusinessUnitCreateCommand command);
    Page<BusinessUnitInternal> getBusinessUnitList(BusinessUnitSearchCondition condition, Pageable pageable);
    BusinessUnitInternal updateStatus(Long id, UsableStatus status);
    void delete(Long id);
}
