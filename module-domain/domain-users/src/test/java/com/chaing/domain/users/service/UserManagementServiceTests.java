package com.chaing.domain.users.service;

import com.chaing.domain.users.dto.command.UserUpdateCommand;
import com.chaing.domain.users.dto.condition.UserSearchCondition;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import com.chaing.domain.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    @DisplayName("회원 등록")
    void registerUser() {

        // given
        User user = User.builder().email("test@example.com").build();
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // when
        userManagementService.registerUser(user, "rawPassword");

        // then
        verify(userRepository, times(1)).save(user);
        assertEquals("encodedPassword", user.getPassword());
    }

    @Test
    @DisplayName("사원번호 생성")
    void generateEmployeeNumber() {

        // given
        UserRole role = UserRole.HQ;
        when(userRepository.findMaxEmployeeNumberByRole(role)).thenReturn(Optional.empty());

        // when
        String result = userManagementService.generateEmployeeNumber(role);

        // then
        assertEquals("10001", result);
    }

    @Test
    @DisplayName("사원번호 생성 (1 증가 확인)")
    void generateEmployeeNumber_Increment() {

        // given
        UserRole role = UserRole.FRANCHISE;
        when(userRepository.findMaxEmployeeNumberByRole(role)).thenReturn(Optional.of("20009"));

        // when
        String result = userManagementService.generateEmployeeNumber(role);

        // then
        assertEquals("20010", result);
    }

    @Test
    @DisplayName("로그인 ID 생성")
    void generateLoginId() {

        // given
        UserRole role = UserRole.HQ;
        String currentYearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String pattern = "hq" + currentYearMonth;
        when(userRepository.findMaxLoginIdByPattern(anyString())).thenReturn(Optional.of(pattern + "005"));

        // when
        String result = userManagementService.generateLoginId(role);

        // then
        assertEquals(pattern + "006", result);
    }

    @Test
    @DisplayName("회원 목록 조회")
    void getUserList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchCondition condition = new UserSearchCondition(null, null, null, null, null, null, null);

        List<User> users = List.of(User.builder().userId(1L).build());
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.searchUsers(condition, pageable)).thenReturn(userPage);

        // when
        Page<User> result = userManagementService.getUserList(condition, pageable);

        // then
        assertEquals(1, result.getContent().size());
        verify(userRepository, times(1)).searchUsers(condition, pageable);
    }

    @Test
    @DisplayName("회원 아이디로 회원 조회")
    void getUserById() {

        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).username("유저").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = userManagementService.getUserById(userId);

        // then
        assertNotNull(result);
        assertEquals("유저", result.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateUser() {

        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).role(UserRole.HQ).position(UserPosition.SYSTEM_MANAGER).build();
        UserUpdateCommand command = new UserUpdateCommand(
                "수정유저", "new@test.com", "010-1234-5678",
                LocalDate.of(1995, 5, 1), "image",
                UserRole.HQ, UserPosition.SYSTEM_MANAGER, 1L
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = userManagementService.updateUser(userId, command);

        // then
        assertNotNull(result);
        assertEquals("수정유저", result.getUsername());
    }

    @Test
    @DisplayName("회원 상태 변경")
    void updateStatus() {

        // given
        Long userId = 1L;
        User user = spy(User.builder().userId(userId).status(UserStatus.ACTIVE).build());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userManagementService.updateStatus(userId, UserStatus.INACTIVE);

        // then
        verify(user, times(1)).updateStatus(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteUser() {

        // given
        Long userId = 1L;
        User user = spy(User.builder().userId(userId).build());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userManagementService.deleteUser(userId);

        // then
        verify(user, times(1)).delete();
    }
}