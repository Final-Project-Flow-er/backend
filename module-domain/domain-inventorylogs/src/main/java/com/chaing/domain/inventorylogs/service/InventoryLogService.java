package com.chaing.domain.inventorylogs.service;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.ActorProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogResponse;
import com.chaing.domain.inventorylogs.entity.InventoryLog;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.exception.InventoryLogException;
import com.chaing.domain.inventorylogs.exception.InventoryLogtErrorCode;
import com.chaing.domain.inventorylogs.repository.InventoryLogArchiveRepository;
import com.chaing.domain.inventorylogs.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class InventoryLogService {
    private static final int MAX_MERGE_WINDOW_SIZE = 2_000;

    private final InventoryLogRepository inventoryLogRepository;
    private final InventoryLogArchiveRepository inventoryLogArchiveRepository;

    @Value("${app.inventory-log.archive.retention-months:6}")
    private int retentionMonths;

    public InventoryLogListResponse findReturnInboundLogs(Long hqId, LogRequest request, Pageable pageable) {
        LocalDate cutoff = cutoffDate();
        if (isActiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogRepository.findReturnInboundLogs(hqId, request, pageable);
        }
        if (isArchiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogArchiveRepository.findReturnInboundLogs(hqId, request, pageable);
        }

        LogRequest archiveRequest = new LogRequest(request.startDate(), minDate(request.endDate(), cutoff.minusDays(1)),
                request.transactionCode());
        LogRequest activeRequest = new LogRequest(maxDate(request.startDate(), cutoff), request.endDate(),
                request.transactionCode());

        return mergeInventoryLogResponse(pageable,
                p -> inventoryLogArchiveRepository.findReturnInboundLogs(hqId, archiveRequest, p),
                p -> inventoryLogRepository.findReturnInboundLogs(hqId, activeRequest, p));
    }

    public InventoryLogListResponse findReturnOutboundLogs(Long hqId, LogRequest request, Pageable pageable) {
        LocalDate cutoff = cutoffDate();
        if (isActiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogRepository.findReturnOutboundLogs(hqId, request, pageable);
        }
        if (isArchiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogArchiveRepository.findReturnOutboundLogs(hqId, request, pageable);
        }

        LogRequest archiveRequest = new LogRequest(request.startDate(), minDate(request.endDate(), cutoff.minusDays(1)),
                request.transactionCode());
        LogRequest activeRequest = new LogRequest(maxDate(request.startDate(), cutoff), request.endDate(),
                request.transactionCode());

        return mergeInventoryLogResponse(pageable,
                p -> inventoryLogArchiveRepository.findReturnOutboundLogs(hqId, archiveRequest, p),
                p -> inventoryLogRepository.findReturnOutboundLogs(hqId, activeRequest, p));
    }

    public InventoryLogListResponse findDisposalLogs(Long hqId, LogRequest request, Pageable pageable) {
        LocalDate cutoff = cutoffDate();
        if (isActiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogRepository.findDisposalLogs(hqId, request, pageable);
        }
        if (isArchiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogArchiveRepository.findDisposalLogs(hqId, request, pageable);
        }

        LogRequest archiveRequest = new LogRequest(request.startDate(), minDate(request.endDate(), cutoff.minusDays(1)),
                request.transactionCode());
        LogRequest activeRequest = new LogRequest(maxDate(request.startDate(), cutoff), request.endDate(),
                request.transactionCode());

        return mergeInventoryLogResponse(pageable,
                p -> inventoryLogArchiveRepository.findDisposalLogs(hqId, archiveRequest, p),
                p -> inventoryLogRepository.findDisposalLogs(hqId, activeRequest, p));
    }

    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
            FranchiseLogRequest request, Pageable pageable) {
        LocalDate cutoff = cutoffDate();
        if (isActiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogRepository.findFranchiseInboundOutboundLogs(franchiseId, request, pageable);
        }
        if (isArchiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogArchiveRepository.findFranchiseInboundOutboundLogs(franchiseId, request, pageable);
        }

        FranchiseLogRequest archiveRequest = new FranchiseLogRequest(
                request.productName(),
                request.startDate(),
                minDate(request.endDate(), cutoff.minusDays(1)),
                request.transactionCode(),
                request.logType());

        FranchiseLogRequest activeRequest = new FranchiseLogRequest(
                request.productName(),
                maxDate(request.startDate(), cutoff),
                request.endDate(),
                request.transactionCode(),
                request.logType());

        return mergeFranchiseLogResponse(pageable,
                p -> inventoryLogArchiveRepository.findFranchiseInboundOutboundLogs(franchiseId, archiveRequest, p),
                p -> inventoryLogRepository.findFranchiseInboundOutboundLogs(franchiseId, activeRequest, p));
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId,
            FranchiseLogRequest request, Pageable pageable) {
        LocalDate cutoff = cutoffDate();
        if (isActiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogRepository.findFranchiseSalesRefundLogs(franchiseId, request, pageable);
        }
        if (isArchiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogArchiveRepository.findFranchiseSalesRefundLogs(franchiseId, request, pageable);
        }

        FranchiseLogRequest archiveRequest = new FranchiseLogRequest(
                request.productName(),
                request.startDate(),
                minDate(request.endDate(), cutoff.minusDays(1)),
                request.transactionCode(),
                request.logType());

        FranchiseLogRequest activeRequest = new FranchiseLogRequest(
                request.productName(),
                maxDate(request.startDate(), cutoff),
                request.endDate(),
                request.transactionCode(),
                request.logType());

        return mergeFranchiseLogResponse(pageable,
                p -> inventoryLogArchiveRepository.findFranchiseSalesRefundLogs(franchiseId, archiveRequest, p),
                p -> inventoryLogRepository.findFranchiseSalesRefundLogs(franchiseId, activeRequest, p));
    }

    public FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request,
            Pageable pageable) {
        LocalDate cutoff = cutoffDate();
        if (isActiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogRepository.findFactoryInventoryLogs(factoryId, request, pageable);
        }
        if (isArchiveOnly(request.startDate(), request.endDate(), cutoff)) {
            return inventoryLogArchiveRepository.findFactoryInventoryLogs(factoryId, request, pageable);
        }

        FactoryLogRequest archiveRequest = new FactoryLogRequest(
                request.productName(),
                request.logType(),
                request.startDate(),
                minDate(request.endDate(), cutoff.minusDays(1)),
                request.transactionCode());

        FactoryLogRequest activeRequest = new FactoryLogRequest(
                request.productName(),
                request.logType(),
                maxDate(request.startDate(), cutoff),
                request.endDate(),
                request.transactionCode());

        return mergeFactoryLogResponse(pageable,
                p -> inventoryLogArchiveRepository.findFactoryInventoryLogs(factoryId, archiveRequest, p),
                p -> inventoryLogRepository.findFactoryInventoryLogs(factoryId, activeRequest, p));
    }

    public List<ActorProductSalesResponse> getProductSales(List<Long> actorIds, List<Long> productIds,
            ActorType actorType, LogType logType) {
        return inventoryLogRepository.getProductSales(actorIds, productIds, actorType, logType);
    }

    public void recordInventoryLog(List<InventoryLogCreateRequest> logs) {
        List<InventoryLog> entities = logs.stream()
                .filter(request -> {
                    // 폐기 로그는 transactionCode가 null이어서 기존 중복 검증이
                    // 과도하게 동작할 수 있으므로 항상 기록한다.
                    if (request.logType() == LogType.DISPOSAL) {
                        return true;
                    }

                    return !inventoryLogRepository
                            .existsByTransactionCodeAndBoxCodeAndLogTypeAndActorTypeAndActorIdAndDeletedAtIsNull(
                                    request.transactionCode(),
                                    request.boxCode(),
                                    request.logType(),
                                    request.actorType(),
                                    request.actorId());
                })
                .map(this::toEntity)
                .toList();

        if (!entities.isEmpty()) {
            inventoryLogRepository.saveAll(entities);
        }
    }

    private InventoryLog toEntity(InventoryLogCreateRequest request) {
        return InventoryLog.builder()
                .productId(request.productId())
                .productName(request.productName())
                .boxCode(request.boxCode())
                .transactionCode(request.transactionCode())
                .logType(request.logType())
                .quantity(request.quantity())
                .fromLocationType(request.fromLocationType())
                .fromLocationId(request.fromLocationId())
                .toLocationType(request.toLocationType())
                .toLocationId(request.toLocationId())
                .actorType(request.actorType())
                .actorId(request.actorId())
                .build();
    }

    public List<BoxCodeResponse> findBoxCodesByTransactionCode(String transactionCode) {
        return findBoxCodesByTransactionCode(transactionCode, null, null, null);
    }

    public List<BoxCodeResponse> findBoxCodesByTransactionCode(String transactionCode, LocalDate date,
            String productName, LogType logType) {
        // product/logType으로 충분히 범위를 좁힌 경우에는 날짜 필터를 강제하지 않는다.
        // (동일 transactionCode 그룹이 여러 날짜에 걸치는 집계 행에서 박스코드 누락 방지)
        boolean useDateFilter = date != null
                && (productName == null || productName.isBlank())
                && logType == null;

        List<BoxCodeResponse> active = useDateFilter
                ? inventoryLogRepository.findBoxCodesByTransactionCodeAndDate(transactionCode, date, productName,
                        logType)
                : inventoryLogRepository.findBoxCodesByTransactionCode(transactionCode, productName, logType);
        List<BoxCodeResponse> archive = useDateFilter
                ? inventoryLogArchiveRepository.findBoxCodesByTransactionCodeAndDate(transactionCode, date,
                        productName, logType)
                : inventoryLogArchiveRepository.findBoxCodesByTransactionCode(transactionCode, productName, logType);

        return Stream.concat(active.stream(), archive.stream())
                .distinct()
                .toList();
    }

    private InventoryLogListResponse mergeInventoryLogResponse(Pageable pageable,
            PageFetcher<InventoryLogListResponse> archiveFetcher,
            PageFetcher<InventoryLogListResponse> activeFetcher) {
        Pageable mergePageable = mergePageable(pageable);

        InventoryLogListResponse archive = archiveFetcher.fetch(mergePageable);
        InventoryLogListResponse active = activeFetcher.fetch(mergePageable);

        List<InventoryLogResponse> merged = new ArrayList<>();
        merged.addAll(archive.inventoryLogResponses());
        merged.addAll(active.inventoryLogResponses());
        merged.sort(Comparator.comparing(InventoryLogResponse::date,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        long total = archive.totalElements() + active.totalElements();

        return InventoryLogListResponse.builder()
                .inventoryLogResponses(slice(merged, pageable))
                .totalElements(total)
                .totalPages(calcTotalPages(total, pageable.getPageSize()))
                .build();
    }

    private FranchiseInventoryLogListResponse mergeFranchiseLogResponse(Pageable pageable,
            PageFetcher<FranchiseInventoryLogListResponse> archiveFetcher,
            PageFetcher<FranchiseInventoryLogListResponse> activeFetcher) {
        Pageable mergePageable = mergePageable(pageable);

        FranchiseInventoryLogListResponse archive = archiveFetcher.fetch(mergePageable);
        FranchiseInventoryLogListResponse active = activeFetcher.fetch(mergePageable);

        List<FranchiseInventoryLogResponse> merged = new ArrayList<>();
        merged.addAll(archive.franchiseInventoryLogResponseList());
        merged.addAll(active.franchiseInventoryLogResponseList());
        merged.sort(Comparator.comparing(FranchiseInventoryLogResponse::date,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        long total = archive.totalElements() + active.totalElements();

        return FranchiseInventoryLogListResponse.builder()
                .franchiseInventoryLogResponseList(slice(merged, pageable))
                .totalElements(total)
                .totalPages(calcTotalPages(total, pageable.getPageSize()))
                .build();
    }

    private FactoryInventoryLogListResponse mergeFactoryLogResponse(Pageable pageable,
            PageFetcher<FactoryInventoryLogListResponse> archiveFetcher,
            PageFetcher<FactoryInventoryLogListResponse> activeFetcher) {
        Pageable mergePageable = mergePageable(pageable);

        FactoryInventoryLogListResponse archive = archiveFetcher.fetch(mergePageable);
        FactoryInventoryLogListResponse active = activeFetcher.fetch(mergePageable);

        List<FactoryInventoryLogResponse> merged = new ArrayList<>();
        merged.addAll(archive.factoryInventoryLogResponseList());
        merged.addAll(active.factoryInventoryLogResponseList());
        merged.sort(Comparator.comparing(FactoryInventoryLogResponse::date,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        long total = archive.totalElements() + active.totalElements();

        return FactoryInventoryLogListResponse.builder()
                .factoryInventoryLogResponseList(slice(merged, pageable))
                .totalElements(total)
                .totalPages(calcTotalPages(total, pageable.getPageSize()))
                .build();
    }

    private <T> List<T> slice(List<T> rows, Pageable pageable) {
        int pageSize = Math.max(1, pageable.getPageSize());
        long offset = pageable.getOffset();
        if (offset >= rows.size()) {
            return List.of();
        }

        int start = (int) offset;
        int end = Math.min(rows.size(), start + pageSize);
        return rows.subList(start, end);
    }

    private Pageable mergePageable(Pageable pageable) {
        int pageSize = Math.max(1, pageable.getPageSize());
        int page = Math.max(0, pageable.getPageNumber());
        long requestedWindow = (long) (page + 1) * pageSize;
        if (requestedWindow > MAX_MERGE_WINDOW_SIZE) {
            throw new InventoryLogException(InventoryLogtErrorCode.PAGE_WINDOW_TOO_LARGE);
        }
        int mergeSize = (int) Math.max(pageSize, requestedWindow);
        return PageRequest.of(0, mergeSize);
    }

    private int calcTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    private boolean isArchiveOnly(LocalDate startDate, LocalDate endDate, LocalDate cutoff) {
        return endDate != null && endDate.isBefore(cutoff);
    }

    private boolean isActiveOnly(LocalDate startDate, LocalDate endDate, LocalDate cutoff) {
        if (startDate == null && endDate == null) {
            return true;
        }
        return startDate != null && !startDate.isBefore(cutoff);
    }

    private LocalDate cutoffDate() {
        return LocalDate.now().minusMonths(retentionMonths);
    }

    private LocalDate minDate(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isBefore(right) ? left : right;
    }

    private LocalDate maxDate(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    @FunctionalInterface
    private interface PageFetcher<T> {
        T fetch(Pageable pageable);
    }
}
