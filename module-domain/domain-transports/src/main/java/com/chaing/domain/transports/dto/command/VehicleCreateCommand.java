package com.chaing.domain.transports.dto.command;

import com.chaing.domain.transports.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleCreateCommand(

        @NotNull(message = "소속 업체는 필수입니다.")
        Long transportId,

        @NotBlank(message = "차량 번호는 필수입니다.")
        String vehicleNumber,

        @NotBlank(message = "차량 종류는 필수입니다.")
        VehicleType vehicleType,

        @NotBlank(message = "운전자 이름은 필수입니다.")
        String driverName,

        @NotBlank(message = "운전자 전화번호는 필수입니다.")
        String driverPhone,

        @NotNull(message = "최대 적재량은 필수입니다.")
        Long maxLoad
) {
}
