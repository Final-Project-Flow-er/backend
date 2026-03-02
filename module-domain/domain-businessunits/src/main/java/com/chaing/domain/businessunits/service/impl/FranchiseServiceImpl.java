package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.component.BusinessUnitCodeGenerator;
import com.chaing.domain.businessunits.component.DistanceCalculator;
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
    private final BusinessUnitCodeGenerator codeGenerator;
    private final DistanceCalculator distanceCalculator;

    // 가맹점 아이디로 가맹점 조회
    @Override
    public BusinessUnitInternal getById(Long id) {
        return franchiseRepository.findById(id)
                .map(BusinessUnitInternal::from)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
    }

    // 가맹점 정보 수정
    @Override
    public BusinessUnitInternal updateInfo(Long id, BusinessUnitUpdateCommand command) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchise.updateFranchiseInfo(command);
        return BusinessUnitInternal.from(franchise);
    }

    // 가맹점 등록
    @Override
    public BusinessUnitInternal create(BusinessUnitCreateCommand command) {
        String generatedCode = codeGenerator.generateFranchiseCode(command.region());
        Double distance = distanceCalculator.calculate(command.address());
        Franchise franchise = Franchise.from(command, generatedCode, distance);
        franchiseRepository.save(franchise);
        return BusinessUnitInternal.from(franchise);
    }

    // 가맹점 목록 조회
    @Override
    public Page<BusinessUnitInternal> getBusinessUnitList(Pageable pageable) {
        return franchiseRepository.findAll(pageable).map(BusinessUnitInternal::from);
    }

    // 가맹점 상태 변경
    @Override
    public BusinessUnitInternal updateStatus(Long id, UsableStatus status) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchise.updateStatus(status);
        return BusinessUnitInternal.from(franchise);
    }

    // 가맹점 삭제
    @Override
    public void delete(Long id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchiseRepository.delete(franchise);
    }

    // 가맹점 경고 부여
    public BusinessUnitInternal addWarning(Long id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        franchise.addWarning();

        return BusinessUnitInternal.from(franchise);
    }
}
