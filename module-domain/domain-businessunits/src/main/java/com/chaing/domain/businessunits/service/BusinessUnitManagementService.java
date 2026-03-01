package com.chaing.domain.businessunits.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;

public interface BusinessUnitManagementService extends BusinessUnitService {

    void create(BusinessUnitInternal businessUnitInternal);
    void updateStatus(Long id, UsableStatus status);
    void delete(Long id);
}
