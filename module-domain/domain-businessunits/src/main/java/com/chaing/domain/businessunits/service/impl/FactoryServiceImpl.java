package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.repository.FactoryRepository;
import com.chaing.domain.businessunits.service.BusinessUnitManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactoryServiceImpl implements BusinessUnitManagementService {

    private final FactoryRepository factoryRepository;

    // 공장 아이디로 공장 조회
    @Override
    public BusinessUnitInternal getById(Long id) {
        return factoryRepository.findById(id)
                .map(BusinessUnitInternal::from)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
    }

    // 공장 정보 수정
    @Override
    public void updateInfo(Long id, BusinessUnitUpdateCommand command) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        factory.updateFactoryInfo(command);
    }

    // 공장 등록
    @Override
    public void create(BusinessUnitCreateCommand command) {
        Factory factory = Factory.from(command);
        factoryRepository.save(factory);
    }

    // 공장 목록 조회
    @Override
    public Page<BusinessUnitInternal> getBusinessUnitList(Pageable pageable) {
        return factoryRepository.findAll(pageable).map(BusinessUnitInternal::from);
    }

    // 공장 상태 변경
    @Override
    public void updateStatus(Long id, UsableStatus status) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        factory.updateStatus(status);
    }

    // 공장 삭제
    @Override
    public void delete(Long id) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        factory.delete();
    }
}
