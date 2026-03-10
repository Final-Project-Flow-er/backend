package com.chaing.domain.businessunits.service.impl;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Headquarter;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.repository.HeadquarterRepository;
import com.chaing.domain.businessunits.service.BusinessUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
