package com.chaing.api.facade.user;

import com.chaing.api.dto.hq.businessunit.response.BusinessUnitDetailResponse;
import com.chaing.api.dto.user.request.ChangePasswordRequest;
import com.chaing.api.dto.user.request.UpdateMyBusinessUnitInfoRequest;
import com.chaing.api.dto.user.request.UpdateMyInfoRequest;
import com.chaing.api.dto.user.response.MyInfoResponse;
import com.chaing.api.facade.hq.BusinessUnitManagementFacade;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.MinioService;
import com.chaing.domain.businessunits.enums.BusinessUnitType;
import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.MyPageService;
import com.chaing.domain.users.service.UserLogService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacade {

    private final MyPageService myPageService;
    private final UserLogService userLogService;
    private final BusinessUnitManagementFacade businessUnitManagementFacade;
    private final MinioService minioService;

    // 내 정보 조회
    public MyInfoResponse getMyInfo(Long userId) {
        User user = myPageService.getMyInfo(userId);
        String profileImageUrl = minioService.getFileUrl(user.getProfileImageUrl(), BucketName.PROFILES);
        return MyInfoResponse.from(user, profileImageUrl);
    }

    // 내 정보 수정
    @Transactional(rollbackFor = Exception.class)
    public MyInfoResponse updateMyProfile(Long userId, UpdateMyInfoRequest request, MultipartFile profileImage) {
        String savedFileName = null;

        if (profileImage != null && !profileImage.isEmpty()) {
            savedFileName = minioService.uploadFile(profileImage, BucketName.PROFILES);
        }

        MyInfoUpdateCommand command = request.toCommand(savedFileName);
        User updatedUser = myPageService.updateMyProfile(userId, command);
        userLogService.saveLog(updatedUser, userId, UserAction.INFO_UPDATE);

        String profileImageUrl = minioService.getFileUrl(updatedUser.getProfileImageUrl(), BucketName.PROFILES);
        return MyInfoResponse.from(updatedUser, profileImageUrl);
    }

    // 비밀번호 변경
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, ChangePasswordRequest request) {
        User user = myPageService.updatePassword(userId, request.toCommand());
        userLogService.saveLog(user, userId, UserAction.PASSWORD_UPDATE);
    }

    // 내 사업장 정보 조회
    public BusinessUnitDetailResponse getMyBusinessUnitInfo(Long userId) {
        User user = myPageService.getMyInfo(userId);
        BusinessUnitType unitType = validateAndGetUnitType(user.getRole(), user.getBusinessUnitId());
        return businessUnitManagementFacade.getDetail(unitType, user.getBusinessUnitId());
    }

    // 내 사업장 정보 수정
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse updateMyBusinessUnitInfo(Long userId, UpdateMyBusinessUnitInfoRequest request) {
        User user = myPageService.getMyInfo(userId);
        BusinessUnitType unitType = validateAndGetUnitType(user.getRole(), user.getBusinessUnitId());
        return businessUnitManagementFacade.updateInfo(unitType, user.getBusinessUnitId(), request.toManagementRequest());
    }

    // Role과 BusinessUnitType 매핑
    private static final Map<UserRole, BusinessUnitType> ROLE_TYPE_MAP = Map.of(
            UserRole.HQ, BusinessUnitType.HQ,
            UserRole.FRANCHISE, BusinessUnitType.FRANCHISE,
            UserRole.FACTORY, BusinessUnitType.FACTORY
    );

    // 역할과 사업장 존재 검증
    private BusinessUnitType validateAndGetUnitType(UserRole role, Long businessUnitId) {
        BusinessUnitType unitType = ROLE_TYPE_MAP.get(role);
        if (unitType == null || businessUnitId == null) {
            throw new UserException(UserErrorCode.INVALID_BUSINESS_UNIT_ACCESS);
        }
        return unitType;
    }
}
