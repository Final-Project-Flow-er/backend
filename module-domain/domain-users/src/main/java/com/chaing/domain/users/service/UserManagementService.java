package com.chaing.domain.users.service;

import com.chaing.domain.users.dto.command.UserUpdateCommand;
import com.chaing.domain.users.entity.User;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 등록
    public void registerUser(User user, String rawPassword) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
        }
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

        return userRepository.findMaxLoginIdByRole(role)
                .map(maxId -> {
                    int nextNum = Integer.parseInt(maxId.substring(2)) + 1;
                    return prefix + String.format("%04d", nextNum);
                })
                .orElse(prefix + "0001");
    }

    // 회원 목록 조회
    public Page<User> getUserList(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // 회원 아이디로 회원 조회
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

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

        if (!user.getEmail().equals(command.email())) {
            if (userRepository.existsByEmail(command.email())) {
                throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
            }
        }

        if (!command.position().isAvailableFor(command.role())) {
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

    public String getPhoneNumberByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getPhone();
    }
}
