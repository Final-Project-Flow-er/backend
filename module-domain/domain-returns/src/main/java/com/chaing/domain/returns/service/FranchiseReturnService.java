package com.chaing.domain.returns.service;

import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemBoxCodeCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCreateCommand;
import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.ReturnAndOrderInfo;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.repository.FranchiseReturnItemBoxCodeRepository;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FranchiseReturnService {

    private final FranchiseReturnRepository franchiseReturnRepository;
    private final FranchiseReturnItemRepository franchiseReturnItemRepository;
    private final FranchiseReturnRepositoryCustom franchiseReturnRepositoryCustom;
    private final FranchiseReturnItemBoxCodeRepository franchiseReturnItemBoxCodeRepository;

    private final ReturnCodeGenerator generator;

    // 반품 전체 조회
    // return: Map<Long, ReturnCommand>
    public Map<Long, ReturnCommand> getAllReturns(Long franchiseId) {
        List<ReturnCommand> returns = franchiseReturnRepositoryCustom.searchAllReturns(franchiseId);

        if (returns == null || returns.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND);
        }

        return returns.stream()
                .collect(Collectors.toMap(
                        ReturnCommand::returnId,
                        Function.identity()
                ));
    }

    // 반품 세부정보 조회
    public ReturnCommand getReturn(Long userId, Long franchiseId, String returnCode) {
        Returns returns = franchiseReturnRepository.findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        return ReturnCommand.from(returns);
    }

    // returnCode로 orderItemId 반환
    public List<Long> getAllReturnItemOrderItemId(String returnCode) {
        return franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode)
                .stream()
                .map(ReturnItem::getFranchiseOrderItemId)
                .toList();
    }

    // 반품 제품 수정
    public List<ReturnItemCommand> updateReturnItems(List<String> boxCodes, Map<Long, Long> orderItemIdByReturnItemId, String returnCode, Map<String, Long> orderItemIdByBoxCode) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        // 저장되어 있던 반품 제품
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        // Map<boxCode, ReturnItem>
        Map<String, ReturnItem> originalItems = items.stream()
                .collect(Collectors.toMap(
                        ReturnItem::getBoxCode,
                        Function.identity()
                ));

        // 수정
        boxCodes.forEach(boxCode -> {
            if (!originalItems.containsKey(boxCode)) {
                // 추가
                ReturnItem returnItem = franchiseReturnItemRepository.save(
                        ReturnItem.builder()
                                .returns(returns)
                                .franchiseOrderItemId(orderItemIdByBoxCode.get(boxCode))
                                .boxCode(boxCode)
                                .build()
                );
                originalItems.put(boxCode, returnItem);
            } else {
                // 삭제
                originalItems.remove(boxCode);
            }
        });

        // 수정사항 반영
        franchiseReturnItemRepository.saveAll(originalItems.values());

        // 반환
        return originalItems.values().stream()
                .map(ReturnItemCommand::from)
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

    // 반품 제품 생성
    public List<ReturnAndOrderInfo> createReturnItems(String returnCode, List<ReturnItemCreateCommand> orderItemIds) {
        // 반품 조회
        Returns returns = franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)
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
        return ReturnAndOrderInfo.from(returnItems);
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

    // 대기 상태의 반품 제품 조회
    // return: Map<returnId, List<ReturnAndOrderInfo>>
    public Map<Long, List<ReturnAndOrderInfo>> getAllReturnItemByStatus(ReturnStatus status) {
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnStatus(status);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getReturns().getReturnId(),
                        Collectors.mapping(ReturnAndOrderInfo::from, Collectors.toList())
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

    // 대기 상태가 아닌 반품 제품 조회
    // Map<returnId, List<ReturnItemInfo>>
    public Map<Long, List<ReturnAndOrderInfo>> getAllNotPendingReturnItem() {
        List<ReturnItem> items = franchiseReturnItemRepository.findAllByReturns_ReturnStatusNot(ReturnStatus.PENDING);

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getReturns().getReturnId(),
                        Collectors.mapping(ReturnAndOrderInfo::from, Collectors.toList())
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
    public Map<Long, List<ReturnItemCommand>> getAllReturnItem() {
        List<ReturnItem> items = franchiseReturnItemRepository.findAllDeletedAtIsNull();

        if (items == null || items.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getReturns().getReturnId(),
                        Collectors.mapping(ReturnItemCommand::from, Collectors.toList())
                ));
    }

    // return: Map<returnItemId, List<ReturnItemBoxCodeCommand>>
    public Map<Long, List<ReturnItemBoxCodeCommand>> getReturnItemBoxCodeByReturnItemId(List<Long> returnItemIds) {
        List<ReturnItemBoxCode> boxCodes = franchiseReturnItemBoxCodeRepository.findAllByReturnItem_ReturnItemIdIn(returnItemIds);

        if (boxCodes == null || boxCodes.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_BOX_CODE_NOT_FOUND);
        }

        return boxCodes.stream()
                .collect(Collectors.groupingBy(
                        boxCode -> boxCode.getReturnItem().getReturnItemId(),
                        Collectors.mapping(ReturnItemBoxCodeCommand::from, Collectors.toList())
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
}
