package com.chaing.domain.users.service;

import com.chaing.core.dto.command.UserContactCommand;
import com.chaing.domain.users.dto.command.UserUpdateCommand;
import com.chaing.domain.users.dto.condition.UserSearchCondition;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 등록
    public void registerUser(User user, String rawPassword) {
        user.changePassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    // 사원번호 생성
    public String generateEmployeeNumber(UserRole role) {
        int startNumber = switch (role) {
            case HQ -> 10001;
            case FRANCHISE -> 20001;
            case FACTORY -> 30001;
            default -> throw new UserException(UserErrorCode.INVALID_ROLE);
        };

        return userRepository.findMaxEmployeeNumberByRole(role)
                .map(maxNum -> {
                    int nextNum = Integer.parseInt(maxNum) + 1;
                    return String.valueOf(nextNum);
                })
                .orElse(String.valueOf(startNumber));
    }

    // 로그인 아이디 생성
    public String generateLoginId(UserRole role) {
        String prefix = switch (role) {
            case HQ -> "hq";
            case FRANCHISE -> "fr";
            case FACTORY -> "fa";
            default -> throw new UserException(UserErrorCode.INVALID_ROLE);
        };

        String currentYearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String searchPattern = prefix + currentYearMonth;

        return userRepository.findMaxLoginIdByPattern(searchPattern)
                .map(maxId -> {
                    int nextNum = Integer.parseInt(maxId.substring(searchPattern.length())) + 1;
                    return searchPattern + String.format("%03d", nextNum);
                })
                .orElse(searchPattern + "001");
    }

    // 회원 목록 조회
    public Page<User> getUserList(UserSearchCondition condition, Pageable pageable) {
        return userRepository.searchUsers(condition, pageable);
    }

    // 회원 아이디로 회원 조회
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    // 활성화 상태인 유저 아이디 전체 조회
    public List<Long> getAllActiveUserIds() {
        return userRepository.getAllActiveUserIds();
    }

    // 로그인 아이디로 회원 조회
    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    // 회원 정보 수정
    public User updateUser(Long userId, UserUpdateCommand command) {
        User user = getUserById(userId);

        UserPosition positionToValidate = (command.position() != null) ? command.position() : user.getPosition();
        UserRole roleToValidate = (command.role() != null) ? command.role() : user.getRole();

        if (!positionToValidate.isAvailableFor(roleToValidate)) {
            throw new UserException(UserErrorCode.INVALID_POSITION_FOR_ROLE);
        }

        user.updateUserInfo(command);
        return user;
    }

    // 회원 상태 변경
    public void updateStatus(Long userId, UserStatus status) {
        User user = getUserById(userId);
        user.updateStatus(status);
    }

    // 회원 탈퇴
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.delete();
    }

    // franchiseId 조회
    public Long getFranchiseIdByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getFranchiseId();
    }

    // username 조회
    public String getUsernameByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getUsername();
    }

    // 유저 아이디로 전화번호 조회
    public String getPhoneNumberByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getPhone();
    }

    // businessUnitId 조회
    public Long getBusinessUnitIdByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getBusinessUnitId();
    }

    // username, phoneNumber 반환 메소드
    public Map<Long, UserContactCommand> getUserContactInfosByUserIds(List<Long> userIds) {
        List<User> users = userRepository.findAllByUserIdIn(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> new UserContactCommand(
                                user.getUsername(),
                                user.getPhone()
                        )
                ));
    }

    public List<Long> getBusinessUnitIdsByRole(UserRole role) {
        return userRepository.findDistinctBusinessUnitIdsByRole(role);
    }

    public List<Long> getActiveUserIdsByRoleAndBusinessUnitId(UserRole role, Long businessUnitId) {
        return userRepository.findActiveUserIdsByRoleAndBusinessUnitId(role, businessUnitId);
    }

    public List<Long> getActiveUserIdsByRole(UserRole role) {
        return userRepository.findActiveUserIdsByRole(role);
    }

    public boolean verifyPassword(Long userId, String rawPassword) {
        User user = getUserById(userId);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
