package com.chaing.domain.users.service;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.entity.UserLog;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.repository.UserLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLogService {

    private final UserLogRepository userLogRepository;

    // 회원 로그 저장
    public void saveLog(User user, Long actorId, UserAction action) {
        UserLog log = UserLog.builder()
                .targetUserId(user.getUserId())
                .actorId(actorId)
                .action(action)
                .targetUsername(user.getUsername())
                .employeeNumber(user.getEmployeeNumber())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .role(user.getRole())
                .position(user.getPosition())
                .build();

        userLogRepository.save(log);
    }

    // 회원 로그 조회
    public Page<UserLog> getAllUserLogs(Pageable pageable) {
        return userLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
