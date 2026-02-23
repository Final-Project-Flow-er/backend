package com.chaing.domain.sales.service;

import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesProductResponse;
import com.chaing.domain.sales.entity.Sales;
import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.exception.FranchiseSalesErrorCode;
import com.chaing.domain.sales.exception.FranchiseSalesException;
import com.chaing.domain.sales.repository.FranchiseSalesRepository;
import com.chaing.domain.sales.repository.interfaces.FranchiseSalesItemRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseSalesService {

    private final FranchiseSalesItemRepositoryCustom franchiseSalesItemRepositoryCustom;
    private final FranchiseSalesItemRepository franchiseSalesItemRepository;
    private final FranchiseSalesRepository franchiseSalesRepository;

    // 판매 목록 조회
    public List<FranchiseSalesInfoResponse> getAllSales(Long franchiseId) {
        // 판매 기록 조회
        return franchiseSalesItemRepositoryCustom.searchAllSalesItems(franchiseId);
    }

    // 판매 목록 세부 조회
    public FranchiseSalesDetailResponse getSalesDetail(Long franchiseId, String salesCode) {
        // salesCode로 판매 기록 가져옴
        Sales sales = franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)
                .orElseThrow(() -> new FranchiseSalesException(FranchiseSalesErrorCode.SALES_NOT_FOUND));

        // 제품 정보 가져옴
        List<SalesItem> salesItems = franchiseSalesItemRepositoryCustom.searchAllSalesItemsBySalesCode(franchiseId, salesCode);

        return FranchiseSalesDetailResponse.builder()
                .salesCode(sales.getSalesCode())
                .salesDate(sales.getCreatedAt())
                .products(
                        FranchiseSalesProductResponse.from(salesItems)
                )
                .build();
    }

    // 판매 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseSellResponse sell(Long franchiseId, FranchiseSellRequest request) {
        Sales sales = null;
        SalesItem salesItem = null;
        List<SalesItem> salesItems = new ArrayList<>();

        try {
            // 판매 생성
            sales = Sales.builder()
                    .franchiseId(franchiseId)
                    .salesCode(salesCodeGenerator())
                    .quantity(request.totalQuantity())
                    .totalAmount(request.totalAmount())
                    .build();
            franchiseSalesRepository.save(sales);
        } catch (DataIntegrityViolationException e) {
            throw new FranchiseSalesException(FranchiseSalesErrorCode.DUPLICATE_SALES_CODE);
        }

        try {
            // 판매 제품 생성
            for (FranchiseSellItemRequest itemRequest : request.requestList()) {
                salesItem = SalesItem.builder()
                        .sales(sales)
                        .productId(itemRequest.productId())
                        .quantity(itemRequest.quantity())
                        .productCode(itemRequest.productCode())
                        .productName(itemRequest.productName())
                        .lot(itemRequest.lot())
                        .unitPrice(itemRequest.unitPrice())
                        .build();
                salesItems.add(salesItem);
            }
            franchiseSalesItemRepository.saveAll(salesItems);
        } catch (DataIntegrityViolationException e) {
            throw new FranchiseSalesException(FranchiseSalesErrorCode.DUPLICATE_LOT);
        }

        return FranchiseSellResponse.from(sales, salesItems);
    }

    private String salesCodeGenerator() {
        // TODO: 매일 초기화하도록
        return LocalDateTime.now() + String.valueOf(Math.random()*100 + 1);
    }
}
