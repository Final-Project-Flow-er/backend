package com.chaing.api.dto.hq.management.request;

import com.chaing.core.enums.Region;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand.FranchiseCreate;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand.FactoryCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record BusinessUnitCreateRequest(

        @NotBlank(message = "사업장 이름은 필수입니다.")
        String name,

        @NotBlank(message = "사업장 주소는 필수입니다.")
        String address,

        @NotBlank(message = "사업장 전화번호는 필수입니다.")
        String phone,

        @NotBlank(message = "대표자 이름은 필수입니다.")
        String representativeName,

        @NotBlank(message = "사업자 등록 번호는 필수입니다.")
        String businessNumber,

        @NotNull(message = "지역은 필수입니다.")
        Region region,

        FranchiseCreateRequest franchiseCreate,
        FactoryCreateRequest factoryCreate
) {

    public BusinessUnitCreateCommand toCommand() {
        return new BusinessUnitCreateCommand(
                this.name,
                this.address,
                this.phone,
                this.representativeName,
                this.businessNumber,
                this.region,
                this.franchiseCreate != null ? this.franchiseCreate.toCommand() : null,
                this.factoryCreate != null ? this.factoryCreate.toCommand() : null
        );
    }

    public record FranchiseCreateRequest(

            @NotBlank(message = "운영 요일은 필수입니다.")
            String operatingDays,

            @NotNull(message = "운영 시작 시간은 필수입니다.")
            LocalTime openTime,

            @NotNull(message = "운영 종료 시간은 필수입니다.")
            LocalTime closeTime,

            String imageUrl
    ) {
        public FranchiseCreate toCommand() {
            return new FranchiseCreate(operatingDays, openTime, closeTime, imageUrl);
        }
    }

    public record FactoryCreateRequest(

            @Positive(message = "생산 라인 개수는 1개 이상이어야 합니다.")
            int productionLineCount
    ) {
        public FactoryCreate toCommand() {
            return new FactoryCreate(productionLineCount);
        }
    }
}
