package com.chaing.domain.businessunits.service;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;

public interface BusinessUnitService {

    BusinessUnitInternal getById(Long id);
    BusinessUnitInternal updateInfo(Long id, BusinessUnitUpdateCommand command);
}
