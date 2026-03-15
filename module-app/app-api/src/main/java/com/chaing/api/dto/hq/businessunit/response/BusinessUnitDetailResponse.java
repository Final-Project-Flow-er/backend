package com.chaing.api.dto.hq.businessunit.response;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BusinessUnitDetailResponse(

        Long id,
        String code,
        String name,
        String address,
        String phone,
        String representativeName,
        String businessNumber,
        Region region,
        UsableStatus status,
        String unitType,

        FranchiseDetailResponse franchiseDetail,
        FactoryDetailResponse factoryDetail
) {

    public record FileInfo(String originName, String storedName, String url, Long size) {
    }

    public static BusinessUnitDetailResponse from(BusinessUnitInternal internal, List<FileInfo> images) {
        return new BusinessUnitDetailResponse(
                internal.id(),
                internal.code(),
                internal.name(),
                internal.address(),
                internal.phone(),
                internal.representativeName(),
                internal.businessNumber(),
                internal.region(),
                internal.status(),
                internal.unitType(),
                internal.franchiseDetail() != null ? FranchiseDetailResponse.from(internal.franchiseDetail(), images) : null,
                internal.factoryDetail() != null ? FactoryDetailResponse.from(internal.factoryDetail()) : null
        );
    }

    public record FranchiseDetailResponse(
            String operatingDays,
            LocalTime openTime,
            LocalTime closeTime,
            List<FileInfo> images,
            int warningCount,
            LocalDateTime penaltyEndDate,
            boolean isReturnBlocked,
            Double distanceToFactory
    ) {
        public static FranchiseDetailResponse from(BusinessUnitInternal.FranchiseDetail detail, List<FileInfo> images) {
            return new FranchiseDetailResponse(
                    detail.operatingDays(), detail.openTime(), detail.closeTime(), images,
                    detail.warningCount(), detail.penaltyEndDate(), detail.isReturnBlocked(), detail.distanceToFactory()
            );
        }
    }

    public record FactoryDetailResponse(
            int productionLineCount
    ) {
        public static FactoryDetailResponse from(BusinessUnitInternal.FactoryDetail detail) {
            return new FactoryDetailResponse(detail.productionLineCount());
        }
    }
}
