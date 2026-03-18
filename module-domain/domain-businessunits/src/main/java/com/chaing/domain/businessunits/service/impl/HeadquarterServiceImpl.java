package com.chaing.domain.businessunits.service.impl;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Headquarter;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.repository.HeadquarterRepository;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.service.BusinessUnitService;
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
public class HeadquarterServiceImpl implements BusinessUnitService {

    private final HeadquarterRepository headquarterRepository;

    // 본사 아이디로 본사 조회
    @Override
    public BusinessUnitInternal getById(Long id) {
        return headquarterRepository.findById(id)
                .map(BusinessUnitInternal::from)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
    }

    // 아이디로 이름 조회
    @Override
    public Map<Long, String> getNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();

        List<Object[]> results = headquarterRepository.findNamesByIds(ids);
        return results.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (String) row[1]
        ));
    }

    // 본사 목록 조회
    @Override
    public Page<BusinessUnitInternal> getBusinessUnitList(BusinessUnitSearchCondition condition, Pageable pageable) {
        if (org.springframework.util.StringUtils.hasText(condition.name())) {
            return headquarterRepository.findByNameContainingIgnoreCase(condition.name(), pageable).map(BusinessUnitInternal::from);
        }
        return headquarterRepository.findAll(pageable).map(BusinessUnitInternal::from);
    }

    // 본사 정보 수정
    @Override
    public BusinessUnitInternal updateInfo(Long id, BusinessUnitUpdateCommand command) {
        Headquarter hq = headquarterRepository.findById(id)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));
        hq.updateHqInfo(command);
        return BusinessUnitInternal.from(hq);
    }

    // 본사 코드 조회
    public String getHqCode(Long hqId) {
        Headquarter hq = headquarterRepository.findById(hqId)
                .orElseThrow(() -> new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND));

        return hq.getHqCode();
    }
}
