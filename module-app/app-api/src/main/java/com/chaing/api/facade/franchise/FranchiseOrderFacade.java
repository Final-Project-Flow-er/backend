package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.orders.request.FranchiseOrderCreateRequest;
import com.chaing.api.dto.franchise.orders.request.FranchiseOrderUpdateRequest;
import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.orders.support.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseOrderFacade {

    private final FranchiseOrderService franchiseOrderService;

    // 가맹점 발주 조회
    public List<FranchiseOrderResponse> getAllOrders(String username) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        List<FranchiseOrder> orders = franchiseOrderService.getAllOrders(franchiseId, username);

        return FranchiseOrderResponse.from(orders);
    }

    // 가맹점의 발주 번호에 따른 특정 발주 조회
    public FranchiseOrderResponse getOrder(String username, String orderCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점의 발주 수정
    @Transactional(rollbackFor = Exception.class)
    public FranchiseOrderResponse updateOrder(String username, String orderCode, FranchiseOrderUpdateRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 수령인이 user 목록에 없으면 예외 발생

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        franchiseOrderService.updateOrder(order, request.toFranchiseOrderUpdateCommand());

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점 발주 취소
    @Transactional(rollbackFor = Exception.class)
    public FranchiseOrderResponse cancelOrder(String username, String orderCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        franchiseOrderService.cancelOrder(order);

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점 발주 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderResponse createOrder(String username, FranchiseOrderCreateRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 받아온 ProductCode에 따라 제품 정보 가져와서 넘겨줘야 함
        // 이건 임시임. 나중에 Product 엔티티에서 정보 가져오는 걸로 바꿔줘야 함
        List<ProductInfo> productInfos = request.items().stream()
                .map(item -> { return ProductInfo.builder()
                        .productCode(item.productCode())
                        .productId(1L)
                        .unitPrice(BigDecimal.valueOf(5000))
                        .build(); })
                .toList();

        FranchiseOrder order = franchiseOrderService.createOrder(franchiseId, username, request.toFranchiseOrderCreateCommand(), productInfos);

        return FranchiseOrderResponse.from(order);
    }
}