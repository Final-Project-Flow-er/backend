package com.chaing.domain.businessunits.service;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;

public interface BusinessUnitService {

    BusinessUnitInternal getById(Long id);
    void updateInfo(Long id, BusinessUnitUpdateCommand command);
}
