package com.chaing.domain.products.service;

import com.chaing.domain.products.dto.request.ProductRequest;
import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.request.ProductUpdateRequest;
import com.chaing.domain.products.dto.response.ProductListResponse;
import com.chaing.domain.products.entity.Component;
import com.chaing.domain.products.entity.Product;
import com.chaing.domain.products.entity.ProductComponent;
import com.chaing.domain.products.entity.ProductType;
import com.chaing.domain.products.enums.ProductStatus;
import com.chaing.domain.products.exception.ProductErrorCode;
import com.chaing.domain.products.exception.ProductException;
import com.chaing.domain.products.repository.ComponentRepository;
import com.chaing.domain.products.repository.ProductComponentRepository;
import com.chaing.domain.products.repository.ProductRepository;
import com.chaing.domain.products.repository.ProductTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private ProductTypeRepository productTypeRepository;

        @Mock
        private ProductComponentRepository productComponentRepository;

        @Mock
        private ComponentRepository componentRepository;

        @Spy
        @InjectMocks
        private ProductService productService;

        @DisplayName("모든 상품 조회")
        @Test
        void getAllProducts() {
                // given
                ProductSearchRequest request = ProductSearchRequest.builder()
                                .productCode("OR0101")
                                .name("오리지널 떡볶이 밀키트 순한맛 1,2인분")
                                .status("판매")
                                .sizeCode("01")
                                .build();

                ProductListResponse response = ProductListResponse.builder()
                                .products(List.of())
                                .build();

                when(productRepository.getProducts(request)).thenReturn(response);

                // when
                ProductListResponse result = productService.getProducts(request);

                // then
                verify(productRepository).getProducts(request);
                assertThat(result).isEqualTo(response);
        }

        @DisplayName("상품 추가")
        @Test
        void createProduct() {
                // given
                ProductRequest request = ProductRequest.builder()
                                .productCode("OR0101")
                                .name("오리지널 떡볶이 밀키트 순한맛 1,2인분")
                                .description("맛있음")
                                .imageUrl("img")
                                .price(new BigDecimal("10000"))
                                .costPrice(new BigDecimal("7000"))
                                .supplyPrice(new BigDecimal("8000"))
                                .status("ON_SALE")
                                .kcal(500)
                                .weight(400)
                                .components(List.of("떡", "어묵", "소스"))
                                .build();

                // 메소드 리턴 값 지정
                doReturn(10L).when(productService).existType("OR0101");

                // 구성품 Mocking
                AtomicLong componentIdSeq = new AtomicLong(1L);
                when(componentRepository.findByName(anyString())).thenReturn(Optional.empty());
                when(componentRepository.save(any())).thenAnswer(inv -> {
                        Component c = inv.getArgument(0);
                        ReflectionTestUtils.setField(c, "componentId", componentIdSeq.getAndIncrement());
                        return c;
                });

                // save할 때 DB에서 정해주는 productId 값 지정
                when(productRepository.save(any())).thenAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        ReflectionTestUtils.setField(p, "productId", 10L);
                        return p;
                });

                // when
                productService.createProduct(request);

                // then
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(productCaptor.capture());

                Product saved = productCaptor.getValue();

                assertThat(saved.getProductCode()).isEqualTo("OR0101");
                assertThat(saved.getStatus()).isEqualTo(ProductStatus.ON_SALE);
                assertThat(saved.getProductTypeId()).isEqualTo(10L);
                assertThat(saved.getName()).isEqualTo("오리지널 떡볶이 밀키트 순한맛 1,2인분");

                // 구성품 저장 검증
                ArgumentCaptor<List<ProductComponent>> compCaptor = ArgumentCaptor.forClass(List.class);
                verify(productComponentRepository).saveAll(compCaptor.capture());

                List<ProductComponent> comps = compCaptor.getValue();

                assertThat(comps.get(0).getProductId()).isEqualTo(10L);
                assertThat(comps.get(1).getProductId()).isEqualTo(10L);
                assertThat(comps.get(2).getProductId()).isEqualTo(10L);

                assertThat(comps.get(0).getComponentId()).isEqualTo(1L);
                assertThat(comps.get(1).getComponentId()).isEqualTo(2L);
                assertThat(comps.get(2).getComponentId()).isEqualTo(3L);
        }

        @DisplayName("상품 수정")
        @Test
        void updateProduct() {
                // given
                Long productId = 1L;
                Product product = Product.builder()
                                .productCode("OR0101")
                                .name("기존 상품")
                                .description("기존 설명")
                                .productTypeId(1L)
                                .imageUrl("old.png")
                                .price(new BigDecimal("10000"))
                                .costPrice(new BigDecimal("7000"))
                                .supplyPrice(new BigDecimal("8000"))
                                .status(ProductStatus.ON_SALE)
                                .kcal(500)
                                .weight(400)
                                .build();

                ProductUpdateRequest req = ProductUpdateRequest.builder()
                                .name("수정 상품")
                                .price(BigDecimal.valueOf(12000))
                                .originalPrice(BigDecimal.valueOf(9000))
                                .status("TEMP_SOLD_OUT")
                                .build();
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                // when
                productService.updateProduct(1L, req);

                // then
                assertThat(product.getName()).isEqualTo("수정 상품");
                assertThat(product.getPrice()).isEqualByComparingTo("12000");
                assertThat(product.getCostPrice()).isEqualByComparingTo("9000");
                assertThat(product.getStatus()).isEqualTo(ProductStatus.TEMP_SOLD_OUT);

                assertThat(product.getDescription()).isEqualTo("기존 설명");
                assertThat(product.getSupplyPrice()).isEqualByComparingTo("8000");
                assertThat(product.getKcal()).isEqualTo(500);
        }

        @DisplayName("상품 타입 추가")
        @Test
        void createProductType() {
                // given
                String type = "OR";
                String productName = "오리지널";
                when(productTypeRepository.save(any())).thenAnswer(invocation -> {
                        ProductType pt = invocation.getArgument(0);
                        ReflectionTestUtils.setField(pt, "id", 1L);
                        return pt;
                });
                when(productTypeRepository.existsByProductType(type)).thenReturn(false);

                // when
                productService.createProductTypes(type, productName);

                // then
                ArgumentCaptor<ProductType> productTypeCaptor = ArgumentCaptor.forClass(ProductType.class);
                verify(productTypeRepository).save(productTypeCaptor.capture());
                ProductType saved = productTypeCaptor.getValue();
                assertThat(saved.getProductType()).isEqualTo(type);
                assertThat(saved.getName()).isEqualTo(productName);
                assertThat(saved.getId()).isEqualTo(1L);

        }

        @DisplayName("상품 타입 추가 실패 - 두 자리 영문 코드만 허용")
        @Test
        void createProductTypeFail_InvalidTypeCode() {
                // given
                String invalidType = "ㅢㅏ";
                String productName = "테스트";

                // when & then
                assertThatThrownBy(() -> productService.createProductTypes(invalidType, productName))
                                .isInstanceOf(ProductException.class);

                verify(productTypeRepository, never()).save(any());
        }

        @DisplayName("상품 타입 추가 실패 - 상품 타입 이름이 자모만으로 구성됨")
        @Test
        void createProductTypeFail_InvalidProductTypeName() {
                // given
                String type = "AS";
                String invalidProductName = "ㅁㄴㅇ";

                // when & then
                assertThatThrownBy(() -> productService.createProductTypes(type, invalidProductName))
                                .isInstanceOf(ProductException.class)
                                .extracting(ex -> ((ProductException) ex).getErrorCode())
                                .isEqualTo(ProductErrorCode.INVALID_PRODUCT_TYPE_NAME);

                verify(productTypeRepository, never()).save(any());
        }

        @DisplayName("상품 타입 존재여부")
        @Test
        void existType() {
                // given
                ProductType productType = ProductType.builder()
                                .id(1L)
                                .productType("OR")
                                .name("오리지널")
                                .build();

                when(productTypeRepository.findByProductType("OR"))
                                .thenReturn(Optional.of(productType));

                // when
                Long result = productService.existType("OR0101");

                // then
                assertThat(result).isEqualTo(1L);
                verify(productTypeRepository).findByProductType("OR");
        }

        @DisplayName("구성품 새로고침")
        @Test
        void syncComponents() {
                // given
                Long productId = 1L;

                Product product = Product.builder()
                                .productCode("OR0101")
                                .name("기존상품")
                                .description("설명")
                                .productTypeId(1L)
                                .imageUrl("img")
                                .price(new BigDecimal("10000"))
                                .costPrice(new BigDecimal("7000"))
                                .supplyPrice(new BigDecimal("8000"))
                                .status(ProductStatus.ON_SALE)
                                .kcal(500)
                                .weight(400)
                                .build();

                when(productRepository.findById(productId))
                                .thenReturn(Optional.of(product));

                // 현재 DB 구성품: 1,2,3
                List<ProductComponent> current = List.of(
                                ProductComponent.builder().productId(productId).componentId(1L).build(),
                                ProductComponent.builder().productId(productId).componentId(2L).build(),
                                ProductComponent.builder().productId(productId).componentId(3L).build());

                when(productComponentRepository.findByProductId(productId))
                                .thenReturn(current);

                // 구성품 Mocking
                when(componentRepository.findByName(anyString())).thenAnswer(inv -> {
                        String name = inv.getArgument(0);
                        return Optional.of(com.chaing.domain.products.entity.Component.builder()
                                        .componentId(99L) // Dummy ID
                                        .name(name)
                                        .build());
                });

                // 요청 구성품: 2,3,4
                ProductUpdateRequest req = ProductUpdateRequest.builder()
                                .components(List.of("구성품2", "구성품3", "구성품4"))
                                .build();

                // when
                productService.updateProduct(productId, req);

                // then
                // 삭제 검증 (1 삭제)
                ArgumentCaptor<List<ProductComponent>> deleteCaptor = ArgumentCaptor.forClass(List.class);

                verify(productComponentRepository).deleteAll(deleteCaptor.capture());

                List<ProductComponent> deleted = deleteCaptor.getValue();
                assertThat(deleted.get(0).getComponentId()).isEqualTo(1L);

                // 추가 검증 (4 추가)
                ArgumentCaptor<List<ProductComponent>> addCaptor = ArgumentCaptor.forClass(List.class);

                verify(productComponentRepository).saveAll(addCaptor.capture());

                List<ProductComponent> added = addCaptor.getValue();
                assertThat(added.get(0).getComponentId()).isEqualTo(4L);
                assertThat(added.get(0).getProductId()).isEqualTo(productId);
        }

}
