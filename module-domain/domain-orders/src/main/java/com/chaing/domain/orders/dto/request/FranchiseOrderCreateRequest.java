package com.chaing.domain.orders.dto.request;

import com.chaing.core.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderCreateInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public record FranchiseOrderCreateRequest(
        @NotBlank
        @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "이름은 한글 또는 영문만 입력 가능합니다.")
        String username,

        @NotBlank
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-xxxx-xxxx 형식이어야 합니다.")
        String phoneNumber,

        @NotNull
        LocalDateTime deliveryDate,

        @NotNull
        String deliveryTime,

        @NotBlank
        String address,

        String requirement,

        List<FranchiseOrderCreateRequestItem> items
) {
    public FranchiseOrderCreateCommand toFranchiseOrderCreateCommand() {
        return FranchiseOrderCreateCommand.builder()
                .username(this.username)
                .phoneNumber(this.phoneNumber)
                .deliveryDate(this.deliveryDate)
                .deliveryTime(this.deliveryTime)
                .address(this.address)
                .requirement(this.requirement)
                .items(FranchiseOrderCreateRequest.from(this.items))
                .build();
    }

    private static List<FranchiseOrderCreateInfo> from(List<FranchiseOrderCreateRequestItem> items) {
        return items.stream()
                .map(item -> {
                    return new FranchiseOrderCreateInfo(
                            item.productCode(),
                            item.quantity()
                    );
                })
                .toList();
    }
}
