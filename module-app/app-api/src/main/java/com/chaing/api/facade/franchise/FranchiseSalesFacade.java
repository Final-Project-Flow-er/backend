package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.sales.response.FranchiseSalesResponse;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.sales.dto.request.FranchiseSellItemRequest;
import com.chaing.domain.sales.dto.request.FranchiseSellRequest;
import com.chaing.domain.sales.dto.response.FranchiseSalesCancellationResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellItemResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellResponse;
import com.chaing.domain.sales.service.FranchiseSalesService;
import com.chaing.domain.users.service.UserManagementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseSalesFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final FranchiseSalesService franchiseSalesService;
    private final UserManagementService userManagementService;
    private final FranchiseServiceImpl franchiseService;
    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 미취소 판매 기록 조회
    public List<FranchiseSalesResponse> getAllSales(Long userId) {
        String cacheKey = "sales:fr:all:%d".formatted(userId);
        List<FranchiseSalesResponse> cached = readListCache(cacheKey, new TypeReference<>() {});
        if (cached != null) return cached;

        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        List<FranchiseSalesInfoResponse> sales = franchiseSalesService.getAllSales(franchiseId);

        List<FranchiseSalesResponse> result = FranchiseSalesResponse.from(sales);
        writeCache(cacheKey, result);
        return result;
    }

    // 미취소 판매 기록 페이지네이션 조회
    public Page<FranchiseSalesResponse> getAllSalesPaged(Long userId, Pageable pageable) {
        String cacheKey = "sales:fr:page:%d:%s".formatted(userId, pageableKey(pageable));
        Page<FranchiseSalesResponse> cached = readPageCache(cacheKey, FranchiseSalesResponse.class, pageable);
        if (cached != null) return cached;

        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        Page<FranchiseSalesInfoResponse> page = franchiseSalesService.getAllSalesPage(franchiseId, pageable);

        List<FranchiseSalesResponse> content = page.getContent().stream()
                .map(FranchiseSalesResponse::from)
                .toList();

        Page<FranchiseSalesResponse> result = new PageImpl<>(content, pageable, page.getTotalElements());
        writePageCache(cacheKey, result);
        return result;
    }

    // 취소 판매 기록 조회
    public List<FranchiseSalesResponse> getAllCanceledSales(Long userId) {
        String cacheKey = "sales:fr:canceled:%d".formatted(userId);
        List<FranchiseSalesResponse> cached = readListCache(cacheKey, new TypeReference<>() {});
        if (cached != null) return cached;

        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        List<FranchiseSalesInfoResponse> canceledSales = franchiseSalesService.getAllCanceledSales(franchiseId);

        List<FranchiseSalesResponse> result = FranchiseSalesResponse.from(canceledSales);
        writeCache(cacheKey, result);
        return result;
    }

    // 판매 기록 세부 조회
    public FranchiseSalesDetailResponse getSalesDetail(Long userId, String salesCode) {
        String cacheKey = "sales:fr:detail:%d:%s".formatted(userId, salesCode);
        FranchiseSalesDetailResponse cached = readObjectCache(cacheKey, FranchiseSalesDetailResponse.class);
        if (cached != null) return cached;

        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        FranchiseSalesDetailResponse result = franchiseSalesService.getSalesDetail(franchiseId, salesCode);
        writeCache(cacheKey, result);
        return result;
    }

    // 판매 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseSalesCancellationResponse cancel(Long userId, String salesCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        FranchiseSalesCancellationResponse result = franchiseSalesService.cancel(franchiseId, salesCode);
        evictByPattern("sales:fr:*");
        evictByPattern("inv:fr:*");
        return result;
    }

    // 판매 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseSellResponse sell(Long userId, FranchiseSellRequest request) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // franchiseCode
        String franchiseCode = franchiseService.getById(franchiseId).code();

        FranchiseSellResponse response = franchiseSalesService.sell(franchiseId, franchiseCode, request);

        // List<serialCode>
        List<String> serialCodes = request.requestList().stream().map(FranchiseSellItemRequest::serialCode).toList();

        //재고 차감
        inventoryService.deleteFranchiseInventory(franchiseId, serialCodes);

        evictByPattern("sales:fr:*");
        evictByPattern("inv:fr:*");
        return response;
    }

    private <T> List<T> readListCache(String key, TypeReference<List<T>> typeRef) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;
            return objectMapper.readValue(cached, typeRef);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T readObjectCache(String key, Class<T> clazz) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;
            return objectMapper.readValue(cached, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeCache(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), CACHE_TTL);
        } catch (Exception ignored) {
        }
    }

    private void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    private String pageableKey(Pageable pageable) {
        return "%d:%d:%s".formatted(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort().toString().replace(" ", "") : "-");
    }

    private <T> Page<T> readPageCache(String key, Class<T> itemClass, Pageable pageable) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;

            JsonNode root = objectMapper.readTree(cached);
            List<T> content = objectMapper.readerForListOf(itemClass).readValue(root.path("content"));
            long totalElements = root.path("totalElements").asLong(content.size());

            return new PageImpl<>(content, pageable, totalElements);
        } catch (Exception e) {
            return null;
        }
    }

    private void writePageCache(String key, Page<?> page) {
        try {
            Map<String, Object> payload = Map.of(
                    "content", page.getContent(),
                    "totalElements", page.getTotalElements());
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(payload), CACHE_TTL);
        } catch (Exception ignored) {
        }
    }
}
