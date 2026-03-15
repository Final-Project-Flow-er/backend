package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.businessunit.request.BusinessUnitCreateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitSearchRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitStatusUpdateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitUpdateRequest;
import com.chaing.api.dto.hq.businessunit.response.BusinessUnitDetailResponse;
import com.chaing.api.dto.hq.businessunit.response.BusinessUnitSummaryResponse;
import com.chaing.core.dto.TargetType;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.ImageService;
import com.chaing.core.service.MinioService;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.enums.BusinessUnitType;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.service.BusinessUnitManagementService;
import com.chaing.domain.businessunits.service.BusinessUnitService;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessUnitManagementFacade {

    private final BusinessUnitService headquarterServiceImpl;
    private final FranchiseServiceImpl franchiseServiceImpl;
    private final BusinessUnitManagementService factoryServiceImpl;
    private final MinioService minioService;
    private final ImageService imageService;

    // 사업장 등록
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse createBusinessUnit(BusinessUnitType type, BusinessUnitCreateRequest request) {
        BusinessUnitCreateCommand command = request.toCommand();
        BusinessUnitInternal internal = switch (type) {
            case FRANCHISE -> franchiseServiceImpl.create(command);
            case FACTORY -> factoryServiceImpl.create(command);
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        };
        return getDetail(type, internal.id());
    }

    // 사업장 목록 조회
    public Page<BusinessUnitSummaryResponse> getList(BusinessUnitType type, BusinessUnitSearchRequest request, Pageable pageable) {
        var condition = request.toCondition();
        Page<BusinessUnitInternal> internals = switch (type) {
            case FRANCHISE -> franchiseServiceImpl.getBusinessUnitList(condition,pageable);
            case FACTORY -> factoryServiceImpl.getBusinessUnitList(condition,pageable);
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        };
        return internals.map(BusinessUnitSummaryResponse::from);
    }

    // 사업장 상세 정보 조회
    public BusinessUnitDetailResponse getDetail(BusinessUnitType type, Long id) {
        BusinessUnitInternal internal = switch (type) {
            case HQ -> headquarterServiceImpl.getById(id);
            case FRANCHISE -> franchiseServiceImpl.getById(id);
            case FACTORY -> factoryServiceImpl.getById(id);
        };

        List<BusinessUnitDetailResponse.FileInfo> imageUrls = List.of();
        if (type == BusinessUnitType.FRANCHISE) {
            imageUrls = imageService.getImagesByTarget(TargetType.FRANCHISE, id).stream()
                    .map(file -> new BusinessUnitDetailResponse.FileInfo(
                            file.getOriginName(),
                            file.getStoredName(),
                            minioService.getFileUrl(file.getStoredName(), BucketName.FRANCHISES),
                            file.getFileSize()
                    )).toList();
        }

        return BusinessUnitDetailResponse.from(internal, imageUrls);
    }

    // 사업장 정보 수정
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse updateInfo(BusinessUnitType type, Long id, BusinessUnitUpdateRequest request) {
        BusinessUnitUpdateCommand command = request.toCommand();
        switch (type) {
            case HQ -> headquarterServiceImpl.updateInfo(id, command);
            case FRANCHISE -> franchiseServiceImpl.updateInfo(id, command);
            case FACTORY -> factoryServiceImpl.updateInfo(id, command);
        }
        return getDetail(type, id);
    }

    // 사업장 상태 변경
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse updateStatus(BusinessUnitType type, Long id, BusinessUnitStatusUpdateRequest request) {
        switch (type) {
            case FRANCHISE -> franchiseServiceImpl.updateStatus(id, request.status());
            case FACTORY -> factoryServiceImpl.updateStatus(id, request.status());
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        }
        return getDetail(type, id);
    }

    // 사업장 삭제
    @Transactional(rollbackFor = Exception.class)
    public void deleteBusinessUnit(BusinessUnitType type, Long id) {
        switch (type) {
            case FRANCHISE -> {
                franchiseServiceImpl.delete(id);
                imageService.deleteAllByTarget(TargetType.FRANCHISE, id, BucketName.FRANCHISES);
            }
            case FACTORY -> factoryServiceImpl.delete(id);
            default -> throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
        }
    }

    // 가맹점 사진 관리
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse updateFranchiseImages(Long id, List<String> deleteStoredFileNames, List<MultipartFile> newImages) {
        franchiseServiceImpl.getById(id);

        if (deleteStoredFileNames != null && !deleteStoredFileNames.isEmpty()) {
            for (String storedName : deleteStoredFileNames) {
                imageService.deleteByStoredName(storedName, BucketName.FRANCHISES);
            }
        }
        if (newImages != null && !newImages.isEmpty()) {
            imageService.saveImages(newImages, TargetType.FRANCHISE, id, BucketName.FRANCHISES);
        }

        return getDetail(BusinessUnitType.FRANCHISE, id);
    }

    // 가맹점 경고 부여
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse addWarning(BusinessUnitType type, Long id) {
        if (type != BusinessUnitType.FRANCHISE) {
            throw new BusinessUnitException(BusinessUnitErrorCode.WARNING_ONLY_FOR_FRANCHISE);
        }
        franchiseServiceImpl.addWarning(id);
        return getDetail(BusinessUnitType.FRANCHISE, id);
    }
}
