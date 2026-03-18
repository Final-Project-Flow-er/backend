package com.chaing.domain.sales.service;

import com.chaing.domain.sales.dto.request.FranchiseSellItemRequest;
import com.chaing.domain.sales.dto.request.FranchiseSellRequest;
import com.chaing.domain.sales.dto.response.FranchiseSalesCancellationResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesProductResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellResponse;
import com.chaing.domain.sales.entity.Sales;
import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.exception.FranchiseSalesErrorCode;
import com.chaing.domain.sales.exception.FranchiseSalesException;
import com.chaing.domain.sales.repository.FranchiseSalesItemRepository;
import com.chaing.domain.sales.repository.FranchiseSalesRepository;
import com.chaing.domain.sales.repository.interfaces.FranchiseSalesItemRepositoryCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FranchiseSalesServiceTests {
    @InjectMocks
    private FranchiseSalesService franchiseSalesService;

    @Mock
    private FranchiseSalesRepository franchiseSalesRepository;

    @Mock
    private FranchiseSalesItemRepositoryCustom franchiseSalesItemRepositoryCustom;

    @Mock
    private FranchiseSalesItemRepository franchiseSalesItemRepository;

    @Mock
    private SalesCodeGenerator salesCodeGenerator;

    Long franchiseId;
    String franchiseCode;
    String username;

    Long salesId;
    String salesCode;

    Long salesItemId;
    Long productId;
    String productCode;
    String productName;
    String lot;

    Sales sales;
    SalesItem salesItem;

    FranchiseSalesInfoResponse franchiseSalesInfoResponse;
    FranchiseSalesInfoResponse franchiseCanceledInfoResponse;
    FranchiseSalesDetailResponse franchiseSalesDetailResponse;
    FranchiseSalesProductResponse franchiseSalesProductResponse;
    FranchiseSellRequest franchiseSellRequest;
    FranchiseSellItemRequest franchiseSellItemRequest;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        franchiseCode = "SE";
        username = "test";

        salesId = 1L;
        salesCode = "salesCode";

        salesItemId = 10L;
        productId = 1001L;
        productCode = "productCode";
        productName = "productName";
        lot = "lot";

        franchiseSalesInfoResponse = FranchiseSalesInfoResponse.builder()
                .salesCode("salesCode")
                .salesDate(LocalDateTime.now())
                .productCode("productCode")
                .productName("productName")
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(5000))
                .totalPrice(BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(10)))
                .build();

        franchiseCanceledInfoResponse = FranchiseSalesInfoResponse.builder()
                .salesCode("salesCode")
                .salesDate(LocalDateTime.now())
                .productCode("productCode")
                .productName("productName")
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(5000))
                .totalPrice(BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(10)))
                .build();

        sales = Sales.builder()
                .franchiseId(franchiseId)
                .salesCode(salesCode)
                .quantity(10)
                .totalAmount(BigDecimal.valueOf(50000))
                .isCanceled(false)
                .build();
        ReflectionTestUtils.setField(sales, "salesId", salesId);

        salesItem = SalesItem.builder()
                .sales(sales)
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(5000))
                .lot(lot)
                .build();
        ReflectionTestUtils.setField(salesItem, "salesItemId", salesItemId);

        franchiseSalesDetailResponse = FranchiseSalesDetailResponse.builder()
                .salesCode(salesCode)
                .salesDate(LocalDateTime.now())
                .products(
                        FranchiseSalesProductResponse.from(List.of(salesItem), 10)
                )
                .build();

        franchiseSellItemRequest = FranchiseSellItemRequest.builder()
                .productCode(productCode)
                .productName(productName)
                .productId(productId)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(5000))
                .serialCode("lot")
                .build();
        franchiseSellRequest = FranchiseSellRequest.builder()
                .totalAmount(BigDecimal.valueOf(50000))
                .totalQuantity(10)
                .requestList(List.of(franchiseSellItemRequest))
                .build();
    }

    @Test
    @DisplayName("미취소 판매 목록 조회 - 성공")
    void getAllSales_Success() {
        // given
        given(franchiseSalesItemRepositoryCustom.searchAllSalesItems(franchiseId)).willReturn(List.of(franchiseSalesInfoResponse));

        // when
        List<FranchiseSalesInfoResponse> responses = franchiseSalesService.getAllSales(franchiseId);

        // then
        verify(franchiseSalesItemRepositoryCustom, times(1)).searchAllSalesItems(franchiseId);
        assertEquals("salesCode", responses.get(0).salesCode());
        assertEquals("productCode", responses.get(0).productCode());
    }

    @Test
    @DisplayName("취소 판매 목록 조회 - 성공")
    void getAllCanceledSales_Success() {
        // given
        given(franchiseSalesItemRepositoryCustom.searchAllCanceledSalesItems(franchiseId)).willReturn(List.of(franchiseCanceledInfoResponse));

        // when
        List<FranchiseSalesInfoResponse> responses = franchiseSalesService.getAllCanceledSales(franchiseId);

        // then
        verify(franchiseSalesItemRepositoryCustom, times(1)).searchAllCanceledSalesItems(franchiseId);
        assertEquals("salesCode", responses.get(0).salesCode());
        assertEquals("productCode", responses.get(0).productCode());
    }

    @Test
    @DisplayName("판매 목록 세부 조회 - 성공")
    void getSalesDetail_Success() {
        // given
        given(franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)).willReturn(Optional.of(sales));
        given(franchiseSalesItemRepositoryCustom.searchAllSalesItemsBySalesCode(franchiseId, salesCode)).willReturn(List.of(salesItem));

        // when
        FranchiseSalesDetailResponse response = franchiseSalesService.getSalesDetail(franchiseId, salesCode);

        // then
        verify(franchiseSalesRepository, times(1)).findByFranchiseIdAndSalesCode(franchiseId, salesCode);
        verify(franchiseSalesItemRepositoryCustom, times(1)).searchAllSalesItemsBySalesCode(franchiseId, salesCode);
        assertEquals("salesCode", response.salesCode());
    }

    @Test
    @DisplayName("판매 기록 세부 조회에서 salesCode로 조회되는 판매 기록이 없는 경우 예외 발생")
    void getSalesDetail_Failure() {
        // given
        given(franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)).willReturn(Optional.empty());

        // when & then
        FranchiseSalesException exception = assertThrows(FranchiseSalesException.class, () -> {
            franchiseSalesService.getSalesDetail(franchiseId, salesCode);
        });
        verify(franchiseSalesRepository, times(1)).findByFranchiseIdAndSalesCode(franchiseId, salesCode);
        assertEquals(FranchiseSalesErrorCode.SALES_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("판매 생성 - 성공")
    void createSales_Success() {
        // when
        FranchiseSellResponse response = franchiseSalesService.sell(franchiseId, franchiseCode, franchiseSellRequest);

        // then
        verify(franchiseSalesRepository, times(1)).save(any());
        verify(franchiseSalesItemRepository, times(1)).saveAll(any());
        assertEquals(10, response.totalQuantity());
        assertEquals(BigDecimal.valueOf(50000), response.totalPrice());
        assertEquals("productCode", response.items().get(0).productCode());
        assertEquals("productName", response.items().get(0).productName());
        assertEquals(BigDecimal.valueOf(5000), response.items().get(0).unitPrice());
    }

    @Test
    @DisplayName("동일한 salesCode로 판매 생성 시 예외 발생")
    void createSales_Failure_DUPLICATE_SALES_CODE() {
        // given
        given(salesCodeGenerator.generate(franchiseCode)).willReturn("salesCode");

        given(franchiseSalesRepository.save(any(Sales.class)))
                .willReturn(sales)
                .willThrow(new DataIntegrityViolationException("duplicate salesCode"));

        // when & then
        franchiseSalesService.sell(franchiseId, franchiseCode, franchiseSellRequest);

        FranchiseSalesException exception = assertThrows(FranchiseSalesException.class, () -> {
            franchiseSalesService.sell(franchiseId, franchiseCode, franchiseSellRequest);
        });
        verify(franchiseSalesRepository, times(2)).save(any());
        assertEquals(FranchiseSalesErrorCode.DUPLICATE_SALES_CODE, exception.getErrorCode());
    }

    @Test
    @DisplayName("동일한 Lot으로 판매 생성 시 예외 발생")
    void createSales_Failure_DUPLICATE_LOT() {
        // given
        given(franchiseSalesRepository.save(any(Sales.class))).willReturn(sales);

        willThrow(new DataIntegrityViolationException("duplicate lot"))
                .given(franchiseSalesItemRepository).saveAll(anyList());

        // when & then
        FranchiseSalesException exception = assertThrows(FranchiseSalesException.class, () -> {
            franchiseSalesService.sell(franchiseId, franchiseCode, franchiseSellRequest);
        });
        verify(franchiseSalesRepository, times(1)).save(any());
        verify(franchiseSalesItemRepository, times(1)).saveAll(any());
        assertEquals(FranchiseSalesErrorCode.DUPLICATE_LOT, exception.getErrorCode());
    }

    @Test
    @DisplayName("판매 취소 - 성공")
    void cancelSales_Success() {
        // given
        given(franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)).willReturn(Optional.of(sales));

        // when
        FranchiseSalesCancellationResponse response = franchiseSalesService.cancel(franchiseId, salesCode);

        // then
        verify(franchiseSalesRepository, times(1)).findByFranchiseIdAndSalesCode(franchiseId, salesCode);
        assertEquals(salesCode, response.salesCode());
        assertEquals(BigDecimal.valueOf(50000), response.totalPrice());
    }

    @Test
    @DisplayName("존재하지 않는 판매에 대해 취소 요청할 시 예외 발생")
    void cancelSales_Failure_SALES_NOT_FOUND() {
        // given
        given(franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)).willReturn(Optional.empty());

        // when & then
        FranchiseSalesException exception = assertThrows(FranchiseSalesException.class, () -> {
            franchiseSalesService.cancel(franchiseId, salesCode);
        });
        verify(franchiseSalesRepository, times(1)).findByFranchiseIdAndSalesCode(franchiseId, salesCode);
        assertEquals(FranchiseSalesErrorCode.SALES_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 취소된 판매에 대한 취소 요청 시 예외 발생")
    void cancelSales_Failure_ALREADY_CANCELLED() {
        // given
        Sales cancelledSales = Sales.builder()
                .franchiseId(franchiseId)
                .salesCode(salesCode)
                .quantity(10)
                .totalAmount(BigDecimal.valueOf(50000))
                .isCanceled(true)
                .build();
        given(franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode)).willReturn(Optional.of(cancelledSales));

        // when & then
        FranchiseSalesException exception = assertThrows(FranchiseSalesException.class, () -> {
            franchiseSalesService.cancel(franchiseId, salesCode);
        });
        verify(franchiseSalesRepository, times(1)).findByFranchiseIdAndSalesCode(franchiseId, salesCode);
        assertEquals(FranchiseSalesErrorCode.ALREADY_CANCELLED, exception.getErrorCode());
    }
}
