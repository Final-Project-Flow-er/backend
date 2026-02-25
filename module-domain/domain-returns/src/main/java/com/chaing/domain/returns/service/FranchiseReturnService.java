package com.chaing.domain.returns.service;

import com.chaing.core.dto.returns.FranchiseOrderInfo;
import com.chaing.domain.returns.dto.command.ReturnItemCreateCommand;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnInfo;
import com.chaing.domain.returns.dto.response.FranchiseReturnProductInfo;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.dto.response.ReturnItemInfo;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FranchiseReturnService {

    private final FranchiseReturnRepository franchiseReturnRepository;
    private final FranchiseReturnItemRepository franchiseReturnItemRepository;
    private final FranchiseReturnRepositoryCustom franchiseReturnRepositoryCustom;

    private final ReturnCodeGenerator generator;

    // 반품 전체 조회
    public List<FranchiseReturnAndReturnItemResponse> getAllReturns(Long franchiseId) {
        return franchiseReturnRepositoryCustom.searchAllReturns(franchiseId);
    }

    // 반품 세부정보 조회
    public FranchiseReturnInfo getReturn(String username, Long franchiseId, String returnCode) {
        Returns returns = franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        return FranchiseReturnInfo.from(returns);
    }

    // returnCode로 orderItemId 반환
    public List<Long> getAllReturnItemOrderItemId(String returnCode) {
        return franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode)
                .stream()
                .map(ReturnItem::getFranchiseOrderItemId)
                .toList();
    }

    // 반품 제품 수정
    public List<FranchiseReturnProductInfo> updateReturnItems(List<FranchiseReturnProductInfo> productInfos, String returnCode, Map<String, Long> orderItemIds) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByReturnCode(returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        // 저장되어 있던 반품 제품
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode);

        // 요청받은 반품 제품
        List<ReturnItem> requestedItems = productInfos.stream()
                .map(info -> {
                    return ReturnItem.builder()
                            .returns(returns)
                            .franchiseOrderItemId(orderItemIds.get(info.serialCode()))
                            .build();
                })
                .toList();

        // 요청받은 것과 비교하며 없으면 삭제, 있으면 추가
        // 1. 요청받은 리스트에 없으면 삭제
        items.removeIf(item -> !orderItemIds.containsValue(item.getFranchiseOrderItemId()));
        // 2. 요청받은 리스트에 있는 데이터 추가
        // 2.1. 이미 있는 경우에는 건너뛰기
        requestedItems.stream()
                .filter(req -> items.stream()
                        .noneMatch(existing -> {
                            return Objects.equals(
                                    req.getFranchiseOrderItemId(),
                                    existing.getFranchiseOrderItemId()
                            );
                        }))
                .forEach(items::add);

        // DTO용 orderItemId -> serialCode 매핑
        Map<Long, String> serialCodeByOrderItemId = orderItemIds.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));

        // DTO용 serialCode -> FranchiseReturnProductInfo 매핑
        Map<String, FranchiseReturnProductInfo> productInfoBySerialCode = productInfos.stream()
                .collect(Collectors.toMap(
                        FranchiseReturnProductInfo::serialCode,
                        Function.identity()
                ));

        return items.stream()
                .map(item -> {
                    Long orderItemId = item.getFranchiseOrderItemId();

                    String serialCode = serialCodeByOrderItemId.get(orderItemId);
                    if (serialCode == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    FranchiseReturnProductInfo productInfo = productInfoBySerialCode.get(serialCode);
                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return productInfo;
                })
                .toList();
    }

    // 반품 조회
    public ReturnInfo getReturnInfo(String username, Long franchiseId, String returnCode) {
        return ReturnInfo.from(
                franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)
                        .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND))
        );
    }

    // 반품 취소
    public String cancel(Long franchiseId, String username, String returnCode) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        returns.cancel();

        return returns.getReturnCode();
    }

    // 반품 생성
    public ReturnInfo createReturn(Long franchiseId, FranchiseReturnCreateRequest request, FranchiseOrderInfo orderInfo) {
        // 반품 코드
        String returnCode = generator.generate();

        // 반품 생성
        Returns returns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderInfo.orderId())
                .returnCode(returnCode)
                .username(orderInfo.username())
                .phoneNumber(orderInfo.phoneNumber())
                .description(request.description())
                .totalReturnQuantity(request.items().size())
                .totalReturnAmount(request.totalPrice())
                .build();

        // 반품 저장
        franchiseReturnRepository.save(returns);

        // 결과 반환
        return ReturnInfo.from(returns);
    }

    public List<ReturnItemInfo> createReturnItems(String returnCode, List<ReturnItemCreateCommand> orderItemIds) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByReturnCode(returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        // 반품 제품 생성
        List<ReturnItem> returnItems = orderItemIds.stream()
                .map(id -> {
                    return ReturnItem.builder()
                            .returns(returns)
                            .franchiseOrderItemId(id.orderItemId())
                            .build();
                })
                .toList();

        // 반품 제품 저장
        franchiseReturnItemRepository.saveAll(returnItems);

        // 결과 반환
        return ReturnItemInfo.from(returnItems);
    }
}
