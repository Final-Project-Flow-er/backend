package com.chaing.api.facade.user;

import com.chaing.api.dto.hq.businessunit.response.BusinessUnitDetailResponse;
import com.chaing.api.dto.user.request.ChangePasswordRequest;
import com.chaing.api.dto.user.request.UpdateMyBusinessUnitInfoRequest;
import com.chaing.api.dto.user.request.UpdateMyInfoRequest;
import com.chaing.api.dto.user.response.MyInfoResponse;
import com.chaing.api.facade.hq.BusinessUnitManagementFacade;
import com.chaing.domain.businessunits.enums.BusinessUnitType;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.MyPageService;
import com.chaing.domain.users.service.UserLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacade {

    private final MyPageService myPageService;
    private final UserLogService userLogService;
    private final BusinessUnitManagementFacade businessUnitManagementFacade;

    // 내 정보 조회
    public MyInfoResponse getMyInfo(Long userId) {
        User user = myPageService.getMyInfo(userId);
        return MyInfoResponse.from(user);
    }

    // 내 정보 수정
    @Transactional(rollbackFor = Exception.class)
    public MyInfoResponse updateMyProfile(Long userId, UpdateMyInfoRequest request) {
        User updatedUser = myPageService.updateMyProfile(userId, request.toCommand());
        userLogService.saveLog(updatedUser, userId, UserAction.INFO_UPDATE);
        return MyInfoResponse.from(updatedUser);
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
        BusinessUnitType unitType = validateAndGetUnitType(user);
        return businessUnitManagementFacade.getDetail(unitType, user.getBusinessUnitId());
    }

    // 내 사업장 정보 수정
    @Transactional(rollbackFor = Exception.class)
    public BusinessUnitDetailResponse updateMyBusinessUnitInfo(Long userId, UpdateMyBusinessUnitInfoRequest request) {
        User user = myPageService.getMyInfo(userId);
        BusinessUnitType unitType = validateAndGetUnitType(user);
        return businessUnitManagementFacade.updateInfo(unitType, user.getBusinessUnitId(), request.toManagementRequest());
    }

    // Role과 BusinessUnitType 매핑
    private static final Map<UserRole, BusinessUnitType> ROLE_TYPE_MAP = Map.of(
            UserRole.HQ, BusinessUnitType.HQ,
            UserRole.FRANCHISE, BusinessUnitType.FRANCHISE,
            UserRole.FACTORY, BusinessUnitType.FACTORY
    );

    // 역할과 사업장 존재 검증
    private BusinessUnitType validateAndGetUnitType(User user) {
        BusinessUnitType unitType = ROLE_TYPE_MAP.get(user.getRole());
        if (unitType == null || user.getBusinessUnitId() == null) {
            throw new UserException(UserErrorCode.INVALID_BUSINESS_UNIT_ACCESS);
        }
        return unitType;
    }
}
