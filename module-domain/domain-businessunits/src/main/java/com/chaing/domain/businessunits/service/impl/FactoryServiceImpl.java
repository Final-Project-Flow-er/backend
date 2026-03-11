package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.component.BusinessUnitCodeGenerator;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactoryServiceImpl implements BusinessUnitManagementService {

    private final FactoryRepository factoryRepository;
    private final BusinessUnitCodeGenerator codeGenerator;

    // 공장 아이디로 공장 조회
    @Override
    public BusinessUnitInternal getById(Long id) {
        return factoryRepository.findById(id)
                .map(BusinessUnitInternal::from)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
    }

    // 공장 정보 수정
    @Override
    public BusinessUnitInternal updateInfo(Long id, BusinessUnitUpdateCommand command) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        factory.updateFactoryInfo(command);
        return BusinessUnitInternal.from(factory);
    }

    // 공장 등록
    @Override
    public BusinessUnitInternal create(BusinessUnitCreateCommand command) {
        String generatedCode = codeGenerator.generateFactoryCode();
        Factory factory = Factory.from(command, generatedCode);
        factoryRepository.save(factory);
        return BusinessUnitInternal.from(factory);
    }

    // 공장 목록 조회
    @Override
    public Page<BusinessUnitInternal> getBusinessUnitList(Pageable pageable) {
        return factoryRepository.findAll(pageable).map(BusinessUnitInternal::from);
    }

    // 아이디로 이름 조회
    @Override
    public Map<Long, String> getNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();

        List<Object[]> results = factoryRepository.findNamesByIds(ids);
        return results.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (String) row[1]
        ));
    }

    // 공장 상태 변경
    @Override
    public BusinessUnitInternal updateStatus(Long id, UsableStatus status) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        factory.updateStatus(status);
        return BusinessUnitInternal.from(factory);
    }

    // 공장 삭제
    @Override
    public void delete(Long id) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        factory.delete();
    }
}
