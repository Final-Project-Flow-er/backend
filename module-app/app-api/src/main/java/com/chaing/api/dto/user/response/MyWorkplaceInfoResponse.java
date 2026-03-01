package com.chaing.api.dto.user.response;

import com.chaing.core.enums.Region;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.entity.Headquarter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record MyWorkplaceInfoResponse(

        String type, // 사업장 종류 (HQ/FRANCHISE/FACTORY)
        String code,
        String name,
        String address,
        String phone,
        String representativeName,
        String businessNumber,
        Region region,
        FranchiseDetail franchiseDetail,
        FactoryDetail factoryDetail
) {
    @Builder
    public record FranchiseDetail(
            String operatingDays,
            LocalTime openTime,
            LocalTime closeTime,
            String imageUrl,
            int warningCount,
            LocalDateTime penaltyEndDate,
            boolean isUnderPenalty,
            Double distanceToFactory
    ) {
        public static FranchiseDetail from(Franchise franchise) {
            return FranchiseDetail.builder()
                    .operatingDays(franchise.getOperatingDays())
                    .openTime(franchise.getOpenTime())
                    .closeTime(franchise.getCloseTime())
                    .imageUrl(franchise.getImageUrl())
                    .warningCount(franchise.getWarningCount())
                    .distanceToFactory(franchise.getDistanceToFactory())
                    .penaltyEndDate(franchise.getPenaltyEndDate())
                    .isUnderPenalty(franchise.getPenaltyEndDate() != null &&
                            franchise.getPenaltyEndDate().isAfter(LocalDateTime.now()))
                    .build();
        }
    }

    @Builder
    public record FactoryDetail(
            int productionLineCount
    ) {}

    // 본사 변환
    public static MyWorkplaceInfoResponse from(Headquarter hq) {
        return MyWorkplaceInfoResponse.builder()
                .type("HQ")
                .code(hq.getHqCode())
                .name(hq.getName())
                .address(hq.getAddress())
                .phone(hq.getPhone())
                .representativeName(hq.getRepresentativeName())
                .businessNumber(hq.getBusinessNumber())
                .build();
    }

    // 가맹점 변환
    public static MyWorkplaceInfoResponse from(Franchise franchise) {
        return MyWorkplaceInfoResponse.builder()
                .type("FRANCHISE")
                .code(franchise.getFranchiseCode())
                .name(franchise.getName())
                .address(franchise.getAddress())
                .phone(franchise.getPhone())
                .representativeName(franchise.getRepresentativeName())
                .businessNumber(franchise.getBusinessNumber())
                .region(franchise.getRegion())
                .franchiseDetail(FranchiseDetail.from(franchise))
                .build();
    }

    // 공장 변환
    public static MyWorkplaceInfoResponse from(Factory factory) {
        return MyWorkplaceInfoResponse.builder()
                .type("FACTORY")
                .code(factory.getFactoryCode())
                .name(factory.getName())
                .address(factory.getAddress())
                .phone(factory.getPhone())
                .representativeName(factory.getRepresentativeName())
                .businessNumber(factory.getBusinessNumber())
                .region(factory.getRegion())
                .factoryDetail(new FactoryDetail(factory.getProductionLineCount()))
                .build();
    }
}
