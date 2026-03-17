package com.chaing.api.dto.hq.user.request;

import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateUserRequest(

        @NotBlank(message = "이름은 필수입니다.")
        String username,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @NotNull(message = "권한 선택은 필수입니다.")
        UserRole role,

        @NotNull(message = "역할 선택은 필수입니다.")
        UserPosition position,

        @NotNull(message = "소속 사업장 선택은 필수입니다.")
        Long businessUnitId,

        String profileImageUrl
) {
}
