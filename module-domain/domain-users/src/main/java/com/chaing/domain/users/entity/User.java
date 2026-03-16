package com.chaing.domain.users.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;
import com.chaing.domain.users.dto.command.UserUpdateCommand;
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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
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

    @Column(nullable = false)
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

    @Column(nullable = false)
    private Long businessUnitId;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

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
        if (request.username() != null) this.username = request.username();
        if (request.email() != null) this.email = request.email();
        if (request.phone() != null) this.phone = request.phone();
        if (request.birthDate() != null) this.birthDate = request.birthDate();
        if (request.profileImageUrl() != null) this.profileImageUrl = request.profileImageUrl();
        if (request.position() != null) this.position = request.position();
        if (request.role() != null && this.role != request.role()) {
            if (request.businessUnitId() == null) {
                throw new UserException(UserErrorCode.INVALID_BUSINESS_UNIT_ACCESS);
            }
            this.role = request.role();
            this.businessUnitId = request.businessUnitId();
        } else {
            if (request.businessUnitId() != null) {
                this.businessUnitId = request.businessUnitId();
            }
        }
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