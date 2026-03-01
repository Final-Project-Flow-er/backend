package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
import com.chaing.domain.businessunits.service.BusinessUnitManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FranchiseServiceImpl implements BusinessUnitManagementService {

    private final FranchiseRepository franchiseRepository;

    @Override
    public BusinessUnitInternal getById(Long id) {
        return null;
    }

    @Override
    public void updateInfo(Long id, BusinessUnitUpdateCommand command) {

    }

    @Override
    public void create(BusinessUnitInternal businessUnitInternal) {

    }

    @Override
    public void updateStatus(Long id, UsableStatus status) {

    }

    @Override
    public void delete(Long id) {

    }
}
