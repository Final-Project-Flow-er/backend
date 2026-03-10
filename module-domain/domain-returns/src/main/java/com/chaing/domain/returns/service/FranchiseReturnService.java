package com.chaing.domain.returns.service;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FranchiseReturnService {

    private final FranchiseReturnRepository franchiseReturnRepository;
    private final FranchiseReturnItemRepository franchiseReturnItemRepository;

    private final ReturnCodeGenerator generator;

    // 반품 전체 조회
    // return: Map<Long, ReturnCommand>
    public Map<Long, ReturnCommand> getAllReturns(Long franchiseId) {
        List<Returns> returns = franchiseReturnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId);

        if (returns == null || returns.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND);
        }

        return returns.stream()
                .collect(Collectors.toMap(
                        Returns::getReturnId,
                        ReturnCommand::from
                ));
    }

    // 반품 세부정보 조회
    public ReturnCommand getReturn(Long userId, Long franchiseId, String returnCode) {
        Returns returns = franchiseReturnRepository.findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        return ReturnCommand.from(returns);
    }

    // 반품 제품 수정
    public List<ReturnItemCommand> updateReturnItems(List<String> boxCodes, String returnCode, Map<String, FranchiseInventoryCommand> inventoryByBoxCode) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        // 저장되어 있던 반품 제품
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        // List<boxCode>
        Set<String> originalBoxCodes = items.stream()
                .map(ReturnItem::getBoxCode)
                .collect(Collectors.toSet());

        log.info("inventoryByBoxCode={}", inventoryByBoxCode);

        // Map<orderItemId, List<boxCode>>
        Map<Long, List<String>> boxCodesByOrderItemId = inventoryByBoxCode.values().stream()
                .collect(Collectors.groupingBy(
                        FranchiseInventoryCommand::orderItemId,
                        Collectors.mapping(FranchiseInventoryCommand::boxCode, Collectors.toList())
                ));

        Set<String> requestedBoxCodes = new HashSet<>(boxCodes);

        // 삭제
        List<ReturnItem> itemsToDelete = items.stream()
                .filter(item -> !requestedBoxCodes.contains(item.getBoxCode()))
                .toList();

        log.info("boxCodesByOrderItemId={}", boxCodesByOrderItemId);

        // 추가
        List<ReturnItem> itemsToAdd = boxCodes.stream()
                .filter(boxCode -> !originalBoxCodes.contains(boxCode))
                .map(boxCode -> ReturnItem.builder()
                        .returns(returns)
                        .franchiseOrderItemId(boxCodesByOrderItemId.entrySet().stream()
                                .filter(entry -> entry.getValue().contains(boxCode))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null))
                        .boxCode(boxCode)
                        .build())
                .toList();

        franchiseReturnItemRepository.deleteAll(itemsToDelete);
        franchiseReturnItemRepository.saveAll(itemsToAdd);

        List<ReturnItem> response = franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);

        // 반환
        return response.stream()
                .map(ReturnItemCommand::from)
                .toList();
    }

    // 반품 취소
    public String cancel(Long franchiseId, Long userId, String returnCode) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        returns.cancel();

        return returns.getReturnCode();
    }

    // 반품 생성
    public ReturnCommand createReturn(Long franchiseId, Long orderId, String returnCode, Long userId, FranchiseReturnCreateRequest request) {
        // 반품 생성
        Returns returns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderId)
                .returnCode(returnCode)
                .userId(userId)
                .description(request.description())
                .totalReturnQuantity(request.quantity())
                .totalReturnAmount(request.totalPrice())
                .build();

        // 반품 저장
        franchiseReturnRepository.save(returns);

        // 결과 반환
        return ReturnCommand.from(returns);
    }

    // 반품 제품 생성
    public List<ReturnItemCommand> createReturnItems(Long returnId, Map<Long, String> boxCodeByOrderItemId) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        // 반품 제품 생성
        List<ReturnItem> returnItems = boxCodeByOrderItemId.entrySet().stream()
                .map(entry -> ReturnItem.builder()
                        .returns(returns)
                        .franchiseOrderItemId(entry.getKey())
                        .boxCode(entry.getValue())
                        .build()
                )
                .toList();

        // 반품 제품 저장
        franchiseReturnItemRepository.saveAll(returnItems);

        // 결과 반환
        return ReturnItemCommand.from(returnItems);
    }

    // 대기 상태의 반품 요청 조회
    // return: Map<returnId, HQReturnCommand>
    public Map<Long, HQReturnCommand> getAllReturnByStatus(ReturnStatus status) {
        List<Returns> returns = franchiseReturnRepository.findAllByReturnStatus(status);

        return returns.stream()
                .collect(Collectors.toMap(
                        Returns::getReturnId,
                        HQReturnCommand::from
                ));
    }

    // 대기 상태가 아닌 반품 요청 조회
    // return: Map<returnId, HQReturnCommand>
    public Map<Long, HQReturnCommand> getAllNotPendingReturn() {
        List<Returns> returns = franchiseReturnRepository.findAllByReturnStatusNot(ReturnStatus.PENDING);

        return returns.stream()
                .collect(Collectors.toMap(
                        Returns::getReturnId,
                        HQReturnCommand::from
                ));
    }

    // 본사 특정 반품 조회
    public HQReturnDetailCommand getHQReturnInfo(String returnCode) {
        return HQReturnDetailCommand.from(
                franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)
                        .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND))
        );
    }

    // return: Map<returnItemId, orderItemId>
    public Map<Long, Long> getReturnItemId(String returnCode) {
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(
                        ReturnItem::getReturnItemId,
                        ReturnItem::getFranchiseOrderItemId
                ));
    }

    // 반품 제품 검수 상태 반환
    // Map<returnItemId, ReturnItemInspection>
    public Map<Long, ReturnItemInspection> getReturnItemInspection(List<Long> returnItemIds) {
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturnItemIdIn(returnItemIds);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(
                        ReturnItem::getReturnItemId,
                        ReturnItemInspection::from
                ));
    }

    // 반품 제품 검수 상태 업데이트
    // Map<returnItemId, ReturnItemInspection>
    public Map<Long, ReturnItemInspection> updateReturnItemStatus(Map<Long, String> serialCodeByReturnItemId, List<HQReturnUpdateRequest> request) {
        List<Long> returnItemIds = serialCodeByReturnItemId.keySet().stream().toList();
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturnItemIdIn(returnItemIds);
        Map<String, HQReturnUpdateRequest> requestBySerialCode = request.stream()
                .collect(Collectors.toMap(
                        HQReturnUpdateRequest::serialCode,
                        Function.identity()
                ));

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        items.forEach(item -> {
            item.update(requestBySerialCode.get(serialCodeByReturnItemId.get(item.getReturnItemId())));
        });

        return items.stream()
                .collect(Collectors.toMap(
                        ReturnItem::getReturnItemId,
                        ReturnItemInspection::from
                ));
    }

    // 반품 요청 상태 변경
    public List<ReturnInfo> updateReturnStatus(List<@NotBlank String> returnCodes) {
        List<Returns> items = franchiseReturnRepository.findAllByReturnCodeIn(returnCodes);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND);
        }

        items.forEach(Returns::updateStatus);

        return items.stream()
                .map(ReturnInfo::from)
                .toList();
    }

    // return: Map<returnId, List<ReturnItemCommand>>
    public Map<Long, List<ReturnItemCommand>> getAllReturnItemByReturnIds(List<Long> returnIds) {
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getReturns().getReturnId(),
                        Collectors.mapping(ReturnItemCommand::from, Collectors.toList())
                ));
    }

    // return: Map<returnItemId, ReturnItemCommand>
    public Map<Long, ReturnItemCommand> getReturnItemsByReturnId(Long returnId) {
        List<ReturnItem> items = franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(
                        ReturnItem::getReturnItemId,
                        ReturnItemCommand::from
                ));
    }


    // InventoryLogFacade에서 사용 - 반품 엔티티 조회 (returnId 기반)
    public Returns getReturnByReturnId(Long returnId) {
        return franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)
                .orElseThrow(() -> new FranchiseReturnException(
                        FranchiseReturnErrorCode.RETURN_NOT_FOUND));
    }

    // InventoryLogFacade에서 사용 - 반품 아이템 리스트 조회 (returnId 기반)
    public List<ReturnItem> getReturnItemListByReturnId(Long returnId) {
        List<ReturnItem> items = franchiseReturnItemRepository
                .findByReturns_ReturnIdAndDeletedAtIsNull(returnId);
        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }
        return items;
    }
}
