package com.chaing.domain.businessunits.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessUnitManagementService extends BusinessUnitService {

    void create(BusinessUnitCreateCommand command);
    Page<BusinessUnitInternal> getBusinessUnitList(Pageable pageable);
    void updateStatus(Long id, UsableStatus status);
    void delete(Long id);
}
