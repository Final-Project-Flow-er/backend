package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementLogRequest;
import com.chaing.api.dto.hq.settlement.response.HQSettlementLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementLogFacade {

    private final com.chaing.domain.settlements.service.SettlementLogService logService;

    // 1. 본사 정산 로그(이력) 리스트 페이징 조회
    public Page<HQSettlementLogResponse> getSettlementLogs(HQSettlementLogRequest request) {
        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 20;

        // 정렬 처리 (기본값: 최신순 createdAt,desc)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (request.sort() != null && !request.sort().isEmpty()) {
            String[] sortParams = request.sort().split(",");
            if (sortParams.length == 2 && sortParams[1].equalsIgnoreCase("asc")) {
                sort = Sort.by(Sort.Direction.ASC, sortParams[0]);
            } else {
                sort = Sort.by(Sort.Direction.DESC, sortParams[0]);
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // type이 ALL이거나 null인 경우는 전체 타입으로 간주하므로 Service 호출 시 type 필터 제외 (null 전달)
        com.chaing.domain.settlements.enums.SettlementLogType typeFilter = null;
        if (request.type() != null && !request.type().name().equals("ALL")) {
            typeFilter = request.type();
        }

        Page<com.chaing.domain.settlements.entity.SettlementLog> logs = logService.getAllByConditions(
                request.franchiseId(), typeFilter, pageable);

        List<HQSettlementLogResponse> dtos = logs.stream()
                .map(log -> HQSettlementLogResponse.of(
                        log.getSettlementLogId(),
                        log.getType(),
                        "가맹점명 (추후 연동)", // TODO: Franchise API 연동
                        log.getContent(),
                        log.getActorName(),
                        log.getCreatedAt()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, logs.getTotalElements());
    }

}
