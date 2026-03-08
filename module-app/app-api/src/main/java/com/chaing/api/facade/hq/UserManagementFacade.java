package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.user.request.CreateUserRequest;
import com.chaing.api.dto.hq.user.request.UpdateUserRequest;
import com.chaing.api.dto.hq.user.request.UserLogSearchRequest;
import com.chaing.api.dto.hq.user.request.UserSearchRequest;
import com.chaing.api.dto.hq.user.response.CreateUserResponse;
import com.chaing.api.dto.hq.user.response.UserDetailResponse;
import com.chaing.api.dto.hq.user.response.UserLogResponse;
import com.chaing.api.dto.hq.user.response.UserSummaryResponse;
import com.chaing.api.dto.user.event.UserInfoResendEvent;
import com.chaing.api.dto.user.event.UserRegisteredEvent;
import com.chaing.core.service.MinioService;
import com.chaing.domain.users.dto.condition.UserLogSearchCondition;
import com.chaing.domain.users.dto.condition.UserSearchCondition;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserStatus;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.AuthService;
import com.chaing.domain.users.service.UserLogService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementFacade {

    private final UserManagementService userManagementService;
    private final AuthService authService;
    private final UserLogService userLogService;
    private final ApplicationEventPublisher eventPublisher;
    private final MinioService minioService;

    // 회원 등록
    @Transactional(rollbackFor = Exception.class)
    public CreateUserResponse registerUser(CreateUserRequest request, MultipartFile profileImage, Long actorId) {

        if (!request.position().isAvailableFor(request.role())) {
            throw new UserException(UserErrorCode.INVALID_POSITION_FOR_ROLE);
        }

        String savedFileName = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            savedFileName = minioService.uploadFile(profileImage, "chaing-profiles");
        }

        String loginId = userManagementService.generateLoginId(request.role());
        String employeeNumber = userManagementService.generateEmployeeNumber(request.role());
        String tempPassword = authService.generateTempPassword();

        User user = User.builder()
                .loginId(loginId)
                .employeeNumber(employeeNumber)
                .username(request.username())
                .email(request.email())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .role(request.role())
                .position(request.position())
                .profileImageUrl(savedFileName)
                .businessUnitId(request.businessUnitId())
                .status(UserStatus.ACTIVE)
                .build();

        userManagementService.registerUser(user, tempPassword);
        userLogService.saveLog(user, actorId, UserAction.REGISTER);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getEmail(), loginId, tempPassword, employeeNumber));

        return CreateUserResponse.from(user);
    }

    // 회원 정보 재발송
    public void sendUserInfo(Long userId) {
        User user = userManagementService.getUserById(userId);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new UserException(UserErrorCode.EMAIL_NOT_FOUND);
        }

        eventPublisher.publishEvent(new UserInfoResendEvent(user.getEmail(), user.getLoginId(), user.getEmployeeNumber()));
    }

    // 회원 목록 조회
    public Page<UserSummaryResponse> getUserList(UserSearchRequest request, Pageable pageable) {
        UserSearchCondition condition = request.toCondition();
        return userManagementService.getUserList(condition, pageable).map(UserSummaryResponse::from);
    }

    // 회원 상세 조회
    public UserDetailResponse getUserById(Long userId) {
        User user = userManagementService.getUserById(userId);
        String profileImageUrl = minioService.getFileUrl(user.getProfileImageUrl(), "chaing-profiles");
        return UserDetailResponse.from(user, profileImageUrl);
    }

    // 회원 정보 수정
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponse updateUser(Long userId, UpdateUserRequest request, MultipartFile profileImage, Long actorId) {

        String savedFileName = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            savedFileName = minioService.uploadFile(profileImage, "chaing-profiles");
        }

        User user = userManagementService.updateUser(userId, request.toCommand(savedFileName));
        userLogService.saveLog(user, actorId, UserAction.INFO_UPDATE);

        String profileImageUrl = minioService.getFileUrl(user.getProfileImageUrl(), "chaing-profiles");
        return UserDetailResponse.from(user, profileImageUrl);
    }

    // 회원 상태 변경
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponse updateUserStatus(Long userId, UserStatus status, Long actorId) {
        userManagementService.updateStatus(userId, status);

        if (status == UserStatus.INACTIVE) {
            authService.deleteRefreshToken(userId);
        }

        User user = userManagementService.getUserById(userId);
        UserAction action = (status == UserStatus.ACTIVE) ? UserAction.RESTORE : UserAction.DEACTIVATE;
        userLogService.saveLog(user, actorId, action);

        String profileImageUrl = minioService.getFileUrl(user.getProfileImageUrl(), "chaing-profiles");
        return UserDetailResponse.from(user, profileImageUrl);
    }

    // 회원 로그 조회
    public Page<UserLogResponse> getUserLogs(UserLogSearchRequest request, Pageable pageable) {
        UserLogSearchCondition condition = request.toCondition();
        return userLogService.getUserLogList(condition, pageable).map(UserLogResponse::from);
    }

    // 회원 삭제
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId, Long actorId) {
        User user = userManagementService.getUserById(userId);
        userLogService.saveLog(user, actorId, UserAction.DELETE);

        authService.deleteRefreshToken(userId);
        userManagementService.deleteUser(userId);
    }
}
