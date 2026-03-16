package com.chaing.domain.users.service;

import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;
import com.chaing.domain.users.dto.command.PasswordUpdateCommand;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    @DisplayName("내 정보 조회")
    void getMyInfo() {

        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).email("test@example.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = myPageService.getMyInfo(userId);

        // then
        assertEquals(userId, result.getUserId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("내 정보 수정")
    void updateMyProfile() {

        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).email("old@example.com").phone("010-0000-0000").build();
        MyInfoUpdateCommand command = new MyInfoUpdateCommand("new@example.com", "010-1234-5678", "new-image.png");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = myPageService.updateMyProfile(userId, command);

        // then
        assertEquals("new@example.com", result.getEmail());
        assertEquals("010-1234-5678", result.getPhone());
        assertEquals("new-image.png", result.getProfileImageUrl());
    }

    @Test
    @DisplayName("비밀번호 변경")
    void updatePassword() {

        // given
        Long userId = 1L;
        User user = spy(User.builder().userId(userId).password("oldPassword").build());
        PasswordUpdateCommand command = new PasswordUpdateCommand("correctPassword", "newPassword!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.currentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(command.newPassword())).thenReturn("newPassword!");

        // when
        myPageService.updatePassword(userId, command);

        // then
        verify(user, times(1)).updatePassword("newPassword!");
        verify(passwordEncoder, times(1)).encode(command.newPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 (현재 비밀번호 불일치 시 예외 발생)")
    void updatePassword_InvalidPassword_ThrowsException() {

        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).password("oldPassword").build();
        PasswordUpdateCommand command = new PasswordUpdateCommand("wrongPassword", "newPassword!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when & then
        assertThrows(UserException.class, () -> myPageService.updatePassword(userId, command));
    }
}