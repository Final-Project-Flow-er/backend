package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.businessunit.request.BusinessUnitCreateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitStatusUpdateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitUpdateRequest;
import com.chaing.api.dto.hq.businessunit.response.BusinessUnitDetailResponse;
import com.chaing.api.dto.hq.businessunit.response.BusinessUnitSummaryResponse;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.enums.BusinessUnitType;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.service.impl.FactoryServiceImpl;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.businessunits.service.impl.HeadquarterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessUnitManagementFacade {

    private final HeadquarterServiceImpl hqService;
    private final FranchiseServiceImpl franchiseService;
    private final FactoryServiceImpl factoryService;

    // 사업장 등록
    @Transactional
    public BusinessUnitDetailResponse createBusinessUnit(BusinessUnitType type, BusinessUnitCreateRequest request) {
        BusinessUnitCreateCommand command = request.toCommand();
        BusinessUnitInternal internal = switch (type) {
            case FRANCHISE -> franchiseService.create(command);
            case FACTORY -> factoryService.create(command);
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        };
        return BusinessUnitDetailResponse.from(internal);
    }

    // 사업장 목록 조회
    public Page<BusinessUnitSummaryResponse> getList(BusinessUnitType type, Pageable pageable) {
        Page<BusinessUnitInternal> internals = switch (type) {
            case FRANCHISE -> franchiseService.getBusinessUnitList(pageable);
            case FACTORY -> factoryService.getBusinessUnitList(pageable);
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        };
        return internals.map(BusinessUnitSummaryResponse::from);
    }

    // 사업장 상세 정보 조회
    public BusinessUnitDetailResponse getDetail(BusinessUnitType type, Long id) {
        BusinessUnitInternal internal = switch (type) {
            case HQ -> hqService.getById(id);
            case FRANCHISE -> franchiseService.getById(id);
            case FACTORY -> factoryService.getById(id);
        };
        return BusinessUnitDetailResponse.from(internal);
    }

    // 사업장 정보 수정
    @Transactional
    public BusinessUnitDetailResponse updateInfo(BusinessUnitType type, Long id, BusinessUnitUpdateRequest request) {
        BusinessUnitUpdateCommand command = request.toCommand();
        BusinessUnitInternal internal = switch (type) {
            case HQ -> hqService.updateInfo(id, command);
            case FRANCHISE -> franchiseService.updateInfo(id, command);
            case FACTORY -> factoryService.updateInfo(id, command);
        };
        return BusinessUnitDetailResponse.from(internal);
    }

    // 사업장 상태 변경
    @Transactional
    public BusinessUnitDetailResponse updateStatus(BusinessUnitType type, Long id, BusinessUnitStatusUpdateRequest request) {
        BusinessUnitInternal internal = switch (type) {
            case FRANCHISE -> franchiseService.updateStatus(id, request.status());
            case FACTORY -> factoryService.updateStatus(id, request.status());
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        };
        return BusinessUnitDetailResponse.from(internal);
    }

    // 사업장 삭제
    @Transactional
    public void deleteBusinessUnit(BusinessUnitType type, Long id) {
        switch (type) {
            case FRANCHISE -> franchiseService.delete(id);
            case FACTORY -> factoryService.delete(id);
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        }
    }

    // 가맹점 경고 부여
    @Transactional
    public BusinessUnitDetailResponse addWarning(BusinessUnitType type, Long id) {
        if (type != BusinessUnitType.FRANCHISE) {
            throw new BusinessUnitException(BusinessUnitErrorCode.WARNING_ONLY_FOR_FRANCHISE);
        }
        BusinessUnitInternal updated = franchiseService.addWarning(id);
        return BusinessUnitDetailResponse.from(updated);
    }
}
