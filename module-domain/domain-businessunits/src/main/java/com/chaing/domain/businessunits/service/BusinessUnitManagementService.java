package com.chaing.domain.businessunits.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;

public interface BusinessUnitManagementService extends BusinessUnitService {

    BusinessUnitInternal create(BusinessUnitCreateCommand command);
    BusinessUnitInternal updateStatus(Long id, UsableStatus status);
    void delete(Long id);
}
