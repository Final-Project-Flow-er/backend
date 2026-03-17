package com.chaing.domain.sales.service;

import com.chaing.domain.sales.dto.request.FranchiseSellItemRequest;
import com.chaing.domain.sales.dto.request.FranchiseSellRequest;
import com.chaing.domain.sales.dto.response.FranchiseSalesCancellationResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesProductResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesDailyQuantityResponse;
import com.chaing.domain.sales.entity.Sales;
import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.exception.FranchiseSalesErrorCode;
import com.chaing.domain.sales.exception.FranchiseSalesException;
import com.chaing.domain.sales.repository.FranchiseSalesItemRepository;
import com.chaing.domain.sales.repository.FranchiseSalesRepository;
import com.chaing.domain.sales.repository.interfaces.FranchiseSalesItemRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseSalesService {

    private final FranchiseSalesItemRepositoryCustom franchiseSalesItemRepositoryCustom;
    private final FranchiseSalesItemRepository franchiseSalesItemRepository;
    private final FranchiseSalesRepository franchiseSalesRepository;

    private final SalesCodeGenerator salesCodeGenerator;

    // 미취소 판매 목록 조회
    public List<FranchiseSalesInfoResponse> getAllSales(Long franchiseId) {
        // 미취소 판매 기록 조회
        return franchiseSalesItemRepositoryCustom.searchAllSalesItems(franchiseId);
    }

    // 미취소 판매 목록 페이지네이션 조회
    public Page<FranchiseSalesInfoResponse> getAllSalesPage(Long franchiseId, Pageable pageable) {
        return franchiseSalesItemRepositoryCustom.searchAllSalesItemsPage(franchiseId, pageable);
    }

    public List<FranchiseSalesInfoResponse> getAllCanceledSales(Long franchiseId) {
        // 취소 판매 기록 조회
        return franchiseSalesItemRepositoryCustom.searchAllCanceledSalesItems(franchiseId);
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
                        FranchiseSalesProductResponse.from(salesItems, sales.getQuantity())
                )
                .build();
    }

    // 판매 생성
    public FranchiseSellResponse sell(Long franchiseId, String franchiseCode, FranchiseSellRequest request) {
        Sales sales = null;
        SalesItem salesItem = null;
        List<SalesItem> salesItems = new ArrayList<>();

        try {
            // 판매 생성
            sales = Sales.builder()
                    .franchiseId(franchiseId)
                    .salesCode(salesCodeGenerator.generate(franchiseCode))
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
                        .productCode(itemRequest.productCode())
                        .productName(itemRequest.productName())
                        .lot(itemRequest.serialCode())
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

    // 판매 취소
    public FranchiseSalesCancellationResponse cancel(Long franchiseId, String salesCode) {
        Sales sales = franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)
                .orElseThrow(() -> new FranchiseSalesException(FranchiseSalesErrorCode.SALES_NOT_FOUND));

        sales.cancel();

        return FranchiseSalesCancellationResponse.from(sales);
    }

    // 안전재고 계산용 판매 집계 조회
    public List<FranchiseSalesDailyQuantityResponse> getDailyProductSalesForSafetyStock(
            List<Long> franchiseIds,
            List<Long> productIds,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return franchiseSalesItemRepositoryCustom.searchDailyProductSalesForSafetyStock(
                franchiseIds, productIds, startDate, endDate);
    }
}
