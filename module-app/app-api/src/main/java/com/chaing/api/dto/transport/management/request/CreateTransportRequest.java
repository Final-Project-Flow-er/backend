package com.chaing.api.dto.transport.management.request;

import com.chaing.core.enums.Region;
import com.chaing.domain.transports.dto.command.TransportCreateCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTransportRequest(

        @NotBlank(message = "운송 업체 이름은 필수입니다.")
        String companyName,

        @NotBlank(message = "대표명은 필수입니다.")
        String manager,

        @NotBlank(message = "업체 전화번호는 필수입니다.")
        String officePhone,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotNull(message = "보유 차량 개수는 필수입니다.")
        Integer ownedVehicles,

        @NotNull(message = "운송 단가는 필수입니다.")
        Long unitPrice,

        @NotNull(message = "계약 시작일은 필수입니다.")
        LocalDate contractStartDate,

        @NotNull(message = "계약 종료일은 필수입니다.")
        LocalDate contractEndDate,

        @NotNull(message = "주력 운송 지역은 필수입니다.")
        Region usableRegion
) {
    @AssertTrue(message = "계약 종료일은 시작일 이후여야 합니다.")
    public boolean isContractPeriodValid() {
        if (contractStartDate == null || contractEndDate == null) {
            return true;
        }
        return !contractEndDate.isBefore(contractStartDate);
    }

    public TransportCreateCommand toCommand() {
        return new TransportCreateCommand(
                this.companyName,
                this.manager,
                this.officePhone,
                this.address,
                this.ownedVehicles,
                this.unitPrice,
                this.contractStartDate,
                this.contractEndDate,
                this.usableRegion
        );
    }
}
