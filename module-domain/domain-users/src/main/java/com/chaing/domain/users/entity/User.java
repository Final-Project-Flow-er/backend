package com.chaing.domain.users.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.users.command.MyInfoUpdateCommand;
import com.chaing.domain.users.command.UserUpdateCommand;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, nullable = false)
    private String employeeNumber;

    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserPosition position;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private Long businessUnitId;

    public Long getHqId() {
        return role == UserRole.HQ ? businessUnitId : null;
    }

    public Long getFranchiseId() {
        return role == UserRole.FRANCHISE ? businessUnitId : null;
    }

    public Long getFactoryId() {
        return role == UserRole.FACTORY ? businessUnitId : null;
    }

    public void changePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD_FORMAT);
        }
        this.password = password;
    }

    public void updateUserInfo(UserUpdateCommand request) {
        this.username = request.username();
        this.email = request.email();
        this.phone = request.phone();
        this.birthDate = request.birthDate();
        this.profileImageUrl = request.profileImageUrl();
        this.role = request.role();
        this.position = request.position();
        this.businessUnitId = request.businessUnitId();
    }

    public void updateMyProfile(MyInfoUpdateCommand command) {
        this.email = command.email();
        this.phone = command.phone();
        this.profileImageUrl = command.profileImageUrl();
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}