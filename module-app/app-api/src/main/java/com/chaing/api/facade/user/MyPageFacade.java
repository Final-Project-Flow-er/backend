package com.chaing.api.facade.user;

import com.chaing.api.dto.user.request.ChangePasswordRequest;
import com.chaing.api.dto.user.request.UpdateMyInfoRequest;
import com.chaing.api.dto.user.request.UpdateMyWorkplaceInfoRequest;
import com.chaing.api.dto.user.response.MyInfoResponse;
import com.chaing.api.dto.user.response.MyWorkplaceInfoResponse;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.service.MyPageService;
import com.chaing.domain.users.service.UserLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacade {

    private final MyPageService myPageService;
    private final UserLogService userLogService;

    // 내 정보 조회
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userId) {
        User user = myPageService.getMyInfo(userId);
        return MyInfoResponse.from(user);
    }

    // 내 정보 수정
    @Transactional
    public MyInfoResponse updateMyProfile(Long userId, UpdateMyInfoRequest request) {
        User updatedUser = myPageService.updateMyProfile(userId, request.toCommand());
        userLogService.saveLog(updatedUser, userId, UserAction.INFO_UPDATE);
        return MyInfoResponse.from(updatedUser);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(Long userId, ChangePasswordRequest request) {
        User user = myPageService.updatePassword(userId, request.toCommand());
        userLogService.saveLog(user, userId, UserAction.PASSWORD_UPDATE);
    }

    // 내 사업장 정보 조회
    @Transactional(readOnly = true)
    public MyWorkplaceInfoResponse getMyWorkplaceInfo(Long userId) {

        Long businessUnitId = myPageService.getMyBusinessUnitId(userId);

        // TODO: 사업장 서비스가 생기면 해당 ID로 상세 정보 조회 (권한별)
        return MyWorkplaceInfoResponse.builder().build();
    }

    // 내 사업장 정보 수정
    @Transactional
    public MyWorkplaceInfoResponse updateMyWorkplaceInfo(Long userId, UpdateMyWorkplaceInfoRequest request) {

        Long businessUnitId = myPageService.getMyBusinessUnitId(userId);

        // TODO: 사업장 서비스 수정 로직 사용 (권한별)
        return MyWorkplaceInfoResponse.builder().build();
    }
}
