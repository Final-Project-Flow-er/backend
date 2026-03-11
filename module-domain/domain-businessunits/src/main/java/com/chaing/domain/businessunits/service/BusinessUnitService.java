package com.chaing.domain.businessunits.service;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;

import java.util.List;
import java.util.Map;

public interface BusinessUnitService {

    BusinessUnitInternal getById(Long id);
    Map<Long, String> getNamesByIds(List<Long> ids);
    BusinessUnitInternal updateInfo(Long id, BusinessUnitUpdateCommand command);

}
