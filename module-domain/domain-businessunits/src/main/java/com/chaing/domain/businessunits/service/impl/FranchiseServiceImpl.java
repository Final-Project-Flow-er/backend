package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
import com.chaing.domain.businessunits.service.BusinessUnitManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FranchiseServiceImpl implements BusinessUnitManagementService {

    private final FranchiseRepository franchiseRepository;

    // 가맹점 아이디로 가맹점 조회
    @Override
    public BusinessUnitInternal getById(Long id) {
        return franchiseRepository.findById(id)
                .map(BusinessUnitInternal::from)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
    }

    // 가맹점 정보 수정
    @Override
    public void updateInfo(Long id, BusinessUnitUpdateCommand command) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchise.updateFranchiseInfo(command);
    }

    // 가맹점 등록
    @Override
    public void create(BusinessUnitCreateCommand command) {
        Franchise franchise = Franchise.from(command);
        franchiseRepository.save(franchise);
    }

    // 가맹점 목록 조회
    @Override
    public Page<BusinessUnitInternal> getBusinessUnitList(Pageable pageable) {
        return franchiseRepository.findAll(pageable).map(BusinessUnitInternal::from);
    }

    // 가맹점 상태 변경
    @Override
    public void updateStatus(Long id, UsableStatus status) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchise.updateStatus(status);
    }

    // 가맹점 삭제
    @Override
    public void delete(Long id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchiseRepository.delete(franchise);
    }
}
