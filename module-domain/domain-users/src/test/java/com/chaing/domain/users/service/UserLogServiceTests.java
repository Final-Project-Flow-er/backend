package com.chaing.domain.users.service;

import com.chaing.domain.users.dto.condition.UserLogSearchCondition;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.entity.UserLog;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.repository.UserLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLogServiceTests {

    @Mock
    private UserLogRepository userLogRepository;

    @InjectMocks
    private UserLogService userLogService;

    @Test
    @DisplayName("회원 로그 저장")
    void saveLog() {

        // given
        User targetUser = User.builder().userId(1L).username("유저").employeeNumber("10001").email("test@example.com").build();
        Long actorId = 2L;
        UserAction action = UserAction.REGISTER;

        ArgumentCaptor<UserLog> logCaptor = ArgumentCaptor.forClass(UserLog.class);

        // when
        userLogService.saveLog(targetUser, actorId, action);

        // then
        verify(userLogRepository, times(1)).save(logCaptor.capture());

        UserLog savedLog = logCaptor.getValue();

        assertEquals(targetUser.getUserId(), savedLog.getTargetUserId());
        assertEquals(actorId, savedLog.getActorId());
        assertEquals(action, savedLog.getAction());
    }

    @Test
    @DisplayName("회원 로그 조회")
    void getAllUserLogs() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        UserLogSearchCondition condition = new UserLogSearchCondition(null, null, null, null, null, null, null, null);

        List<UserLog> logs = List.of(UserLog.builder().build(), UserLog.builder().build());
        Page<UserLog> expectedPage = new PageImpl<>(logs);

        when(userLogRepository.searchUserLogs(condition, pageable)).thenReturn(expectedPage);

        // when
        Page<UserLog> result = userLogService.getUserLogList(condition, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(userLogRepository, times(1)).searchUserLogs(condition, pageable);
    }
}