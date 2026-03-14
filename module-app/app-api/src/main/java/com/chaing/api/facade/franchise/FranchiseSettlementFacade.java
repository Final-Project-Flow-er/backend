package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.settlement.response.FranchiseDailyGraphResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseOrderItemResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseSalesItemResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseSettlementSummaryResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseVoucherResponse;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.repository.FranchiseOrderItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.repository.FranchiseSalesItemRepository;
import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.enums.DocumentOwner;
import com.chaing.domain.settlements.enums.DocumentType;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.SettlementStatus;
import com.chaing.domain.settlements.enums.VoucherType;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import com.chaing.domain.settlements.service.DailySettlementService;
import com.chaing.domain.settlements.service.MonthlySettlementService;
import com.chaing.domain.settlements.service.SettlementDocumentService;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.SettlementVoucherRepository;
import com.chaing.domain.settlements.entity.SettlementVoucher;
import com.chaing.domain.settlements.service.SettlementFileService;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.MinioService;
import com.chaing.domain.settlements.entity.SettlementDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FranchiseSettlementFacade {

        private final DailySettlementService dailyService;
        private final MonthlySettlementService monthlyService;
        private final SettlementDocumentService documentService;
        private final MinioService minioService;
        private final SettlementFileService fileService;
        private final SettlementVoucherRepository voucherRepository;

        // 다른 도메인 Repository (메서드 추가 요청 후 사용)
        private final FranchiseSalesItemRepository salesItemRepository;
        private final FranchiseOrderRepository orderRepository;
        private final FranchiseOrderItemRepository orderItemRepository;
        private final FranchiseReturnRepository returnRepository;
        private final FranchiseReturnItemRepository returnItemRepository;

        // 일별 정산 요약
        @Transactional(readOnly = true)
        public FranchiseSettlementSummaryResponse getDailySummary(Long franchiseId, LocalDate date) {
                return toSummary(getInternalDailyReceipt(franchiseId, date));
        }

        // 내부 합산용 일별 정산 조회 (실시간 또는 DB)
        private DailySettlementReceipt getInternalDailyReceipt(Long franchiseId, LocalDate date) {
                // 당일 요청인 경우, DB에 저장된 예전 기록(있을 경우)보다 실시간 집계를 우선함
                if (date.equals(LocalDate.now())) {
                        log.info("[DEBUG] Requesting today's settlement for franchiseId: {}. Performing real-time aggregation.",
                                        franchiseId);
                        return aggregateDailySettlement(franchiseId, date);
                }

                DailySettlementReceipt receipt = dailyService.findByFranchiseAndDate(franchiseId, date).orElse(null);

                if (receipt == null) {
                        log.info("[DEBUG] Daily receipt not found in DB for franchiseId: {}, date: {}. Aggregating on the fly.",
                                        franchiseId, date);
                        return aggregateDailySettlement(franchiseId, date);
                }

                // 이미 DB에 기록이 있더라도 매출액이나 발주금액이 0이거나, 수수료가 집계되지 않은 경우 실시간으로 다시 집계함
                if (receipt.getTotalSaleAmount().compareTo(BigDecimal.ZERO) == 0 ||
                                (receipt.getTotalSaleAmount().compareTo(BigDecimal.ZERO) > 0
                                                && receipt.getCommissionFee().compareTo(BigDecimal.ZERO) == 0)) {
                        log.info("[DEBUG] Persistent receipt exists but needs update (0 values or missing commission). Re-aggregating for franchiseId: {}, date: {}",
                                        franchiseId, date);
                        return aggregateDailySettlement(franchiseId, date);
                }

                return receipt;
        }

        // 일별 매출 top5, 전체
        @Transactional(readOnly = true)
        public List<FranchiseSalesItemResponse> getDailySalesItems(
                        Long franchiseId, LocalDate date, Integer limit) {
                log.info("[DEBUG] getDailySalesItems - franchiseId: {}, date: {}",
                                franchiseId, date);

                // 명시적인 필터링을 위해 전체 조회 후 toLocalDate()로 필터링 (정합성 극대화)
                List<SalesItem> items = salesItemRepository.findAllBySalesFranchiseId(franchiseId).stream()
                                .filter(item -> {
                                        LocalDateTime createdAt = item.getCreatedAt();
                                        return createdAt != null && createdAt.toLocalDate().equals(date);
                                })
                                .filter(item -> {
                                        Boolean canceled = item.getSales().getIsCanceled();
                                        return canceled == null || !canceled;
                                })
                                .toList();

                log.info("[DEBUG] getDailySalesItems - found items count: {}", items.size());

                // 상품별 집계 (상품명 기준 그룹핑)
                return aggregateSalesItems(items, limit);
        }

        // 일별 발주 top5, 전체
        @Transactional(readOnly = true)
        public List<FranchiseOrderItemResponse> getDailyOrderItems(
                        Long franchiseId, LocalDate date, Integer limit) {
                List<FranchiseOrderStatus> validStatuses = List.of(
                                FranchiseOrderStatus.PENDING,
                                FranchiseOrderStatus.ACCEPTED,
                                FranchiseOrderStatus.PARTIAL,
                                FranchiseOrderStatus.SHIPPING_PENDING,
                                FranchiseOrderStatus.SHIPPING,
                                FranchiseOrderStatus.COMPLETED);

                // DB 레벨 조회가 아닌, 전체 데이터를 가져와 toLocalDate()로 필터링 (정합성 보장)
                List<FranchiseOrder> orders = orderRepository.findAllByFranchiseId(franchiseId).stream()
                                .filter(order -> {
                                        LocalDateTime createdAt = order.getCreatedAt();
                                        return createdAt != null && createdAt.toLocalDate().equals(date);
                                })
                                .filter(order -> validStatuses.contains(order.getOrderStatus()))
                                .toList();
                // OrderItem 가져오기
                List<Long> orderIds = orders.stream()
                                .map(FranchiseOrder::getFranchiseOrderId).toList();
                List<FranchiseOrderItem> items = orderItemRepository
                                .findAllByFranchiseOrderFranchiseOrderIdIn(orderIds);
                // 상품별 집계
                return aggregateOrderItems(items, limit);
        }

        // 월별 정산 요약
        @Transactional(readOnly = true)
        public FranchiseSettlementSummaryResponse getMonthlySummary(Long franchiseId, YearMonth month) {
                try {
                        MonthlySettlement s = monthlyService.getByFranchiseAndMonth(franchiseId, month);
                        return new FranchiseSettlementSummaryResponse(
                                        s.getFinalSettlementAmount(),
                                        s.getTotalSaleAmount(),
                                        s.getRefundAmount(),
                                        s.getOrderAmount(),
                                        s.getDeliveryFee(),
                                        s.getLossAmount(),
                                        s.getCommissionFee(),
                                        s.getAdjustmentAmount());
                } catch (SettlementException e) {
                        if (e.getErrorCode() == SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND) {
                                log.info("[DEBUG] Monthly settlement not found for franchiseId: {}, month: {}. Aggregating on the fly.",
                                                franchiseId, month);
                                // 해당 월의 모든 날짜(1일~말일 또는 오늘까지)에 대해 일별 정산 가집계 수행
                                LocalDate start = month.atDay(1);
                                LocalDate today = LocalDate.now();
                                YearMonth currentMonth = YearMonth.now();
                                LocalDate end;

                                if (month.equals(currentMonth)) {
                                        end = today;
                                } else {
                                        end = month.atEndOfMonth();
                                }

                                List<DailySettlementReceipt> dailyReceipts = new ArrayList<>();
                                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                                        try {
                                                // getDailySummary 내부 로직과 유사하게 가져오되, Receipt 객체로 합산
                                                dailyReceipts.add(getInternalDailyReceipt(franchiseId, date));
                                        } catch (Exception ex) {
                                                log.warn("[DEBUG] Skip aggregation for date {} due to: {}", date,
                                                                ex.getMessage());
                                        }
                                }

                                if (dailyReceipts.isEmpty()) {
                                        throw e; // 여전히 데이터가 없으면 원래 에러 던짐
                                }

                                return toSummary(aggregateMonthlySettlement(franchiseId, month, dailyReceipts));
                        }
                        throw e;
                }
        }

        // 월별 매출 현황 top5, 전체
        @Transactional(readOnly = true)
        public List<FranchiseSalesItemResponse> getMonthlySalesItems(
                        Long franchiseId, YearMonth month, Integer limit) {
                LocalDateTime start = month.atDay(1).atStartOfDay();
                LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59, 999999999);

                // SalesItem에서 직접 조회 후 메모리 필터링
                List<SalesItem> items = salesItemRepository.findAllBySalesFranchiseId(franchiseId).stream()
                                .filter(item -> {
                                        LocalDateTime createdAt = item.getCreatedAt();
                                        return createdAt != null && !createdAt.isBefore(start)
                                                        && !createdAt.isAfter(end);
                                })
                                .filter(item -> {
                                        Boolean canceled = item.getSales().getIsCanceled();
                                        return canceled == null || !canceled;
                                })
                                .toList();

                return aggregateSalesItems(items, limit);
        }

        // 월별 발주 top5, 전체
        @Transactional(readOnly = true)
        public List<FranchiseOrderItemResponse> getMonthlyOrderItems(
                        Long franchiseId, YearMonth month, Integer limit) {
                LocalDate start = month.atDay(1);
                LocalDate end = month.atEndOfMonth();
                List<FranchiseOrderStatus> validStatuses = List.of(
                                FranchiseOrderStatus.PENDING,
                                FranchiseOrderStatus.ACCEPTED,
                                FranchiseOrderStatus.PARTIAL,
                                FranchiseOrderStatus.SHIPPING_PENDING,
                                FranchiseOrderStatus.SHIPPING,
                                FranchiseOrderStatus.COMPLETED);

                List<FranchiseOrder> orders = orderRepository
                                .findAllByFranchiseIdAndOrderStatusInAndCreatedAtBetween(
                                                franchiseId,
                                                validStatuses,
                                                start.atStartOfDay(),
                                                end.atTime(23, 59, 59));
                List<Long> orderIds = orders.stream()
                                .map(FranchiseOrder::getFranchiseOrderId).toList();
                List<FranchiseOrderItem> items = orderItemRepository
                                .findAllByFranchiseOrderFranchiseOrderIdIn(orderIds);
                return aggregateOrderItems(items, limit);
        }

        // 월별 일자 매출 추이 그래프
        @Transactional(readOnly = true)
        public List<FranchiseDailyGraphResponse> getMonthlyDailyGraph(
                        Long franchiseId, LocalDate start, LocalDate end) {
                List<DailySettlementReceipt> receipts = dailyService.getAllByFranchiseAndDateRange(franchiseId, start,
                                end);
                return receipts.stream()
                                .map(r -> new FranchiseDailyGraphResponse(
                                                r.getSettlementDate(),
                                                r.getTotalSaleAmount()))
                                .sorted(Comparator.comparing(FranchiseDailyGraphResponse::date))
                                .toList();
        }

        // 전ㅍ 상세 목록 (일, 월)
        @Transactional(readOnly = true)
        public Page<FranchiseVoucherResponse> getVouchers(
                        Long franchiseId, PeriodType period,
                        LocalDate date, YearMonth month,
                        VoucherType type, Pageable pageable) {
                log.info("[DEBUG] getVouchers - franchiseId={}, period={}, date={}, month={}",
                                franchiseId, period, date, month);
                if (period == PeriodType.DAILY) {
                        DailySettlementReceipt receipt = dailyService.findByFranchiseAndDate(franchiseId, date)
                                        .orElse(null);

                        // 과거 데이터인 경우 DB에서 조회
                        if (receipt != null && !date.equals(LocalDate.now())) {
                                Page<DailyReceiptLine> lines = dailyService.getReceiptLines(
                                                receipt.getDailyReceiptId(),
                                                type,
                                                pageable);
                                return lines.map(this::toVoucherResponse);
                        }
                        // 오늘 데이터거나 DB에 없으면 실시간 가집계
                        return getProvisionalVouchers(franchiseId, date, type, pageable);
                } else {
                        // MONTHLY → SettlementVoucher 조회
                        MonthlySettlement settlement = monthlyService.getByFranchiseAndMonth(franchiseId, month);
                        Page<SettlementVoucher> vouchers;
                        if (type != null) {
                                vouchers = voucherRepository.findAllByMonthlySettlementIdAndVoucherType(
                                                settlement.getMonthlySettlementId(), type, pageable);
                        } else {
                                vouchers = voucherRepository.findAllByMonthlySettlementId(
                                                settlement.getMonthlySettlementId(), pageable);
                        }
                        return vouchers.map(this::toVoucherResponse);
                }
        }
        // 내부 메서드, 외부 controller에서 직접 부를 수 없는 private로 선언.
        // domain으로 보내지 않고 api모듈에서 계속 사용

        // 일별 정산 요약 데이터 받기
        private FranchiseSettlementSummaryResponse toSummary(DailySettlementReceipt r) {
                return new FranchiseSettlementSummaryResponse(
                                r.getFinalAmount(),
                                r.getTotalSaleAmount(),
                                r.getRefundAmount(),
                                r.getOrderAmount(),
                                r.getDeliveryFee(),
                                r.getLossAmount(),
                                r.getCommissionFee(),
                                r.getAdjustmentAmount());
        }

        private FranchiseSettlementSummaryResponse toSummary(MonthlySettlement s) {
                return new FranchiseSettlementSummaryResponse(
                                s.getFinalSettlementAmount(),
                                s.getTotalSaleAmount(),
                                s.getRefundAmount(),
                                s.getOrderAmount(),
                                s.getDeliveryFee(),
                                s.getLossAmount(),
                                s.getCommissionFee(),
                                s.getAdjustmentAmount());
        }

        // 전표 상세 보기 탭 목록
        private FranchiseVoucherResponse toVoucherResponse(DailyReceiptLine line) {
                return new FranchiseVoucherResponse(
                                line.getReferenceCode(),
                                line.getLineType(),
                                line.getDescription(),
                                line.getQuantity(),
                                line.getAmount(),
                                line.getOccurredAt());
        }

        private FranchiseVoucherResponse toVoucherResponse(SettlementVoucher voucher) {
                return new FranchiseVoucherResponse(
                                voucher.getReferenceCode(),
                                voucher.getVoucherType(),
                                voucher.getDescription(),
                                voucher.getQuantity(),
                                voucher.getAmount(),
                                voucher.getOccurredAt());
        }

        /**
         * 오늘 날짜의 실시간 전표 목록을 생성하여 반환함
         */
        private Page<FranchiseVoucherResponse> getProvisionalVouchers(
                        Long franchiseId, LocalDate date, VoucherType filterType, Pageable pageable) {
                List<FranchiseVoucherResponse> allVouchers = new ArrayList<>();

                // 1. 매출 (Sales)
                if (filterType == null || filterType == VoucherType.SALES) {

                        List<SalesItem> salesItems = salesItemRepository.findAllBySalesFranchiseId(franchiseId).stream()
                                        .filter(item -> {
                                                LocalDateTime createdAt = item.getCreatedAt();
                                                return createdAt != null && createdAt.toLocalDate().equals(date);
                                        })
                                        .filter(item -> {
                                                Boolean canceled = item.getSales().getIsCanceled();
                                                return canceled == null || !canceled;
                                        })
                                        .toList();

                        for (SalesItem item : salesItems) {
                                allVouchers.add(new FranchiseVoucherResponse(
                                                item.getSales().getSalesCode(),
                                                VoucherType.SALES,
                                                item.getProductName() + " (판매)",
                                                item.getQuantity(),
                                                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                                item.getCreatedAt()));
                        }
                }

                // 2. 발주 (Order)
                if (filterType == null || filterType == VoucherType.ORDER) {
                        List<FranchiseOrderStatus> validOrderStatuses = List.of(
                                        FranchiseOrderStatus.PENDING,
                                        FranchiseOrderStatus.ACCEPTED,
                                        FranchiseOrderStatus.PARTIAL,
                                        FranchiseOrderStatus.SHIPPING_PENDING,
                                        FranchiseOrderStatus.SHIPPING,
                                        FranchiseOrderStatus.COMPLETED);
                        List<FranchiseOrder> orderList = orderRepository.findAllByFranchiseId(franchiseId).stream()
                                        .filter(o -> {
                                                LocalDateTime createdAt = o.getCreatedAt();
                                                return createdAt != null && createdAt.toLocalDate().equals(date);
                                        })
                                        .filter(o -> validOrderStatuses.contains(o.getOrderStatus()))
                                        .toList();
                        for (FranchiseOrder o : orderList) {
                                allVouchers.add(new FranchiseVoucherResponse(
                                                o.getOrderCode(),
                                                VoucherType.ORDER,
                                                "가맹점 발주 (승인대기 포함)",
                                                1, // 발주 건수
                                                o.getTotalAmount(),
                                                o.getCreatedAt()));
                        }
                }

                // 3. 반품 (Refund)
                if (filterType == null || filterType == VoucherType.REFUND) {
                        List<ReturnStatus> validReturnStatuses = List.of(
                                        ReturnStatus.PENDING,
                                        ReturnStatus.ACCEPTED,
                                        ReturnStatus.SHIPPING_PENDING,
                                        ReturnStatus.SHIPPING,
                                        ReturnStatus.COMPLETED,
                                        ReturnStatus.INSPECTING,
                                        ReturnStatus.DEDUCTION_COMPLETED);
                        List<Returns> returnList = returnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)
                                        .stream()
                                        .filter(r -> {
                                                LocalDateTime createdAt = r.getCreatedAt();
                                                return createdAt != null && createdAt.toLocalDate().equals(date);
                                        })
                                        .filter(r -> validReturnStatuses.contains(r.getReturnStatus()))
                                        .toList();
                        for (Returns r : returnList) {
                                allVouchers.add(new FranchiseVoucherResponse(
                                                r.getReturnCode(),
                                                VoucherType.REFUND,
                                                "가맹점 반품 (신청대기 포함)",
                                                1,
                                                r.getTotalReturnAmount(),
                                                r.getCreatedAt()));
                        }
                }

                // 발생시간 역순 정렬
                allVouchers.sort((a, b) -> b.occurredAt().compareTo(a.occurredAt()));

                // 페이징 처리
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allVouchers.size());

                if (start > allVouchers.size()) {
                        return new org.springframework.data.domain.PageImpl<>(new ArrayList<>(), pageable,
                                        allVouchers.size());
                }

                return new org.springframework.data.domain.PageImpl<>(
                                allVouchers.subList(start, end), pageable, allVouchers.size());
        }

        // SalesItem 집계: 상품명별 수량/금액 합산 후에 순위 매기기
        private List<FranchiseSalesItemResponse> aggregateSalesItems(
                        List<SalesItem> items, Integer limit) {
                // 상품명별 그룹핑
                Map<String, List<SalesItem>> grouped = items.stream()
                                .collect(Collectors.groupingBy(SalesItem::getProductName));
                List<FranchiseSalesItemResponse> result = grouped.entrySet().stream()
                                .map(entry -> {
                                        String name = entry.getKey();
                                        List<SalesItem> group = entry.getValue();
                                        int totalQty = group.stream()
                                                        .mapToInt(SalesItem::getQuantity).sum();
                                        BigDecimal total = group.stream()
                                                        .map(item -> item.getUnitPrice().multiply(
                                                                        BigDecimal.valueOf(item.getQuantity())))
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal displayPrice = group.get(0).getUnitPrice();
                                        return new FranchiseSalesItemResponse(0, name, totalQty, displayPrice, total);
                                }) // 단가가 변경될 경우
                                .sorted((a, b) -> b.totalSales().compareTo(a.totalSales()))
                                .toList();
                // 순위 부여 + limit 적용
                List<FranchiseSalesItemResponse> ranked = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                        if (limit != null && i >= limit)
                                break;
                        var item = result.get(i);
                        ranked.add(new FranchiseSalesItemResponse(
                                        i + 1, item.productName(), item.totalQuantity(),
                                        item.unitPrice(), item.totalSales()));
                }
                return ranked;
        }

        // OrderItem 집계 (FranchiseOrderItem에는 상품명이 없으므로 productId 사용)
        // 발주 상품별로 묶고 더해서 순위 매기기
        private List<FranchiseOrderItemResponse> aggregateOrderItems(
                        List<FranchiseOrderItem> items, Integer limit) {
                // productId별 그룹핑
                Map<Long, List<FranchiseOrderItem>> grouped = items.stream()
                                .collect(Collectors.groupingBy(FranchiseOrderItem::getProductId));
                List<FranchiseOrderItemResponse> result = grouped.entrySet().stream()
                                .map(entry -> {
                                        Long productId = entry.getKey();
                                        String productName = "품목 ID " + productId; // TODO: 상품 도메인에서 조회 필요
                                        List<FranchiseOrderItem> group = entry.getValue();
                                        int totalQty = group.stream()
                                                        .mapToInt(FranchiseOrderItem::getQuantity).sum();
                                        BigDecimal total = group.stream()
                                                        .map(item -> item.getUnitPrice().multiply(
                                                                        BigDecimal.valueOf(item.getQuantity())))
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal displayPrice = group.get(0).getUnitPrice();
                                        return new FranchiseOrderItemResponse(0, productName, totalQty, displayPrice,
                                                        total);
                                })
                                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                                .toList();
                List<FranchiseOrderItemResponse> ranked = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                        if (limit != null && i >= limit)
                                break;
                        var item = result.get(i);
                        ranked.add(new FranchiseOrderItemResponse(
                                        i + 1, item.productName(), item.totalQuantity(),
                                        item.unitPrice(), item.totalAmount()));
                }
                return ranked;
        }

        // pdf, excel 다운로드 (프론트 통신용 실제 URL 반환) ---
        @Transactional
        public String getDailyReceiptPdf(Long franchiseId, LocalDate date) {
                try {
                        // 1. 해당 가맹점의 일별 정산 데이터 조회
                        DailySettlementReceipt receipt = dailyService.getByFranchiseAndDate(franchiseId, date);

                        // 2. 해당 정산 ID로 이미 생성된 문서가 있는지 확인
                        java.util.Optional<SettlementDocument> existingDoc = documentService
                                        .getDailyDocument(receipt.getDailyReceiptId());
                        if (existingDoc.isPresent()) {
                                return minioService.getFileUrl(existingDoc.get().getObjectKey(),
                                                BucketName.SETTLEMENTS);
                        }

                        // 3. 문서가 없으면 실시간 생성
                        List<DailyReceiptLine> lines = dailyService.getAllReceiptLines(receipt.getDailyReceiptId());
                        byte[] pdfBytes = fileService.createDailyReceiptPdf(receipt, lines);

                        // 4. MinIO 업로드
                        String fileName = "settlement/daily/FR_" + franchiseId + "_Daily_" + date + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);
                        log.info("====> {} ", fileUrl);
                        // 5. DB에 메타데이터 저장
                        SettlementDocument newDoc = SettlementDocument.builder()
                                        .periodType(PeriodType.DAILY)
                                        .documentType(DocumentType.RECEIPT_PDF)
                                        .documentOwner(DocumentOwner.FRANCHISE)
                                        .franchiseId(franchiseId)
                                        .dailyReceiptId(receipt.getDailyReceiptId())
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/pdf")
                                        .fileSize((long) pdfBytes.length)
                                        .build();
                        documentService.save(newDoc);

                        return fileUrl;
                } catch (SettlementException e) {
                        if (e.getErrorCode() == SettlementErrorCode.DAILY_SETTLEMENT_NOT_FOUND) {
                                // 데이터가 없으면 실시간 집계 시도
                                return generateProvisionalDailyPdf(franchiseId, date);
                        }
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate Daily Franchise Receipt PDF: ", e);
                        throw new SettlementException(SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getMonthlyReceiptPdf(Long franchiseId, YearMonth month) {
                try {
                        // 1. 해당 가맹점의 월별 정산 데이터 조회 (없으면 SettlementException 발생)
                        MonthlySettlement settlement;
                        List<SettlementVoucher> vouchers;

                        try {
                                settlement = monthlyService.getByFranchiseAndMonth(franchiseId, month);
                                vouchers = voucherRepository
                                                .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());
                        } catch (SettlementException e) {
                                if (e.getErrorCode() == SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND) {
                                        // 데이터가 없으면 실시간 집계 시도
                                        return generateProvisionalMonthlyPdf(franchiseId, month);
                                }
                                throw e;
                        }

                        // 2. 이미 생성된 문서(RECEIPT_PDF)가 있는지 확인
                        try {
                                List<com.chaing.domain.settlements.entity.SettlementDocument> documents = documentService
                                                .getMonthlyDocuments(settlement.getMonthlySettlementId());

                                String existingUrl = documents.stream()
                                                .filter(doc -> doc.getDocumentType() == DocumentType.RECEIPT_PDF)
                                                .findFirst()
                                                .map(com.chaing.domain.settlements.entity.SettlementDocument::getFileUrl)
                                                .orElse(null);

                                if (existingUrl != null) {
                                        return minioService.getFileUrl(documents.stream()
                                                        .filter(doc -> doc
                                                                        .getDocumentType() == DocumentType.RECEIPT_PDF)
                                                        .findFirst()
                                                        .get().getObjectKey(), BucketName.SETTLEMENTS);
                                }
                        } catch (SettlementException e) {
                                // 문서 목록 조회 시 에러가 나더라도(없으면), 아래에서 실시간 생성 진행을 위해 넘어감
                                if (e.getErrorCode() == SettlementErrorCode.INVALID_SETTLEMENT_ID) {
                                        throw e;
                                }
                        }

                        // 4. 없으면 실시간 생성
                        byte[] pdfBytes = fileService.createMonthlyReceiptPdf(settlement, vouchers);

                        // 5. MinIO 업로드
                        String fileName = "settlement/monthly/Franchise_" + franchiseId + "_Monthly_Receipt_" + month
                                        + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);

                        // 6. DB 메타데이터 저장
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);
                        documentService.save(SettlementDocument.builder()
                                        .monthlySettlementId(settlement.getMonthlySettlementId())
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(DocumentType.RECEIPT_PDF)
                                        .documentOwner(DocumentOwner.FRANCHISE)
                                        .franchiseId(franchiseId)
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/pdf")
                                        .fileSize((long) pdfBytes.length)
                                        .build());

                        return fileUrl;
                } catch (SettlementException e) {
                        if (e.getErrorCode() == SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND) {
                                if (month.isAfter(YearMonth.now().minusMonths(1))) { // 이번 달인 경우 안내 메시지 포함된 코드 사용
                                        throw new SettlementException(
                                                        SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FINALIZED);
                                }
                                throw e;
                        }
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate Monthly Franchise Receipt PDF: ", e);
                        throw new SettlementException(SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getMonthlyVouchersExcel(Long franchiseId, YearMonth month) {
                try {
                        // 1. 해당 가맹점의 월별 정산 데이터 조회
                        MonthlySettlement settlement;
                        try {
                                settlement = monthlyService.getByFranchiseAndMonth(franchiseId, month);
                        } catch (SettlementException e) {
                                if (e.getErrorCode() == SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND) {
                                        return generateProvisionalMonthlyExcel(franchiseId, month);
                                }
                                throw e;
                        }

                        // 2. 이미 생성된 엑셀 문서가 있는지 확인
                        List<SettlementDocument> documents = documentService
                                        .getMonthlyDocuments(settlement.getMonthlySettlementId());
                        if (documents != null) {
                                var existingExcel = documents.stream()
                                                .filter(doc -> doc.getDocumentType() == DocumentType.VOUCHER_EXCEL)
                                                .findFirst();
                                if (existingExcel.isPresent()) {
                                        return minioService.getFileUrl(existingExcel.get().getObjectKey(),
                                                        BucketName.SETTLEMENTS);
                                }
                        }

                        // 3. 없으면 실시간 생성
                        List<SettlementVoucher> vouchers = voucherRepository
                                        .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());
                        byte[] excelBytes = fileService.createMonthlyVoucherExcel(vouchers);

                        // 4. MinIO 업로드
                        String fileName = "settlement/monthly/FR_" + franchiseId + "_Voucher_" + month + "_"
                                        + System.currentTimeMillis() + ".xlsx";
                        minioService.uploadFile(excelBytes, fileName,
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        // 5. DB에 메타데이터 저장
                        SettlementDocument newDoc = SettlementDocument.builder()
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(DocumentType.VOUCHER_EXCEL)
                                        .documentOwner(DocumentOwner.FRANCHISE)
                                        .franchiseId(franchiseId)
                                        .monthlySettlementId(settlement.getMonthlySettlementId())
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                        .fileSize((long) excelBytes.length)
                                        .build();
                        documentService.save(newDoc);

                        return fileUrl;
                } catch (SettlementException e) {
                        if (e.getErrorCode() == SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND) {
                                if (month.isAfter(YearMonth.now().minusMonths(1))) {
                                        throw new SettlementException(
                                                        SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FINALIZED);
                                }
                        }
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate Monthly Franchise Vouchers Excel: ", e);
                        throw new SettlementException(SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        private String generateProvisionalMonthlyPdf(Long franchiseId, YearMonth month) {
                // 1. 해당 월의 모든 일별 정산 데이터 조회
                List<DailySettlementReceipt> dailyReceipts = dailyService.getAllByFranchiseAndDateRange(
                                franchiseId, month.atDay(1), month.atEndOfMonth());

                if (dailyReceipts.isEmpty()) {
                        // 상황 2: 정산 데이터 자체가 없음 (휴무일 등)
                        throw new SettlementException(SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                }

                // 2. 가집계 MonthlySettlement 객체 생성
                MonthlySettlement provisionalSettlement = aggregateMonthlySettlement(franchiseId, month, dailyReceipts);

                // 3. 일별 데이터를 기반으로 가상 전표(Voucher) 생성
                List<SettlementVoucher> provisionalVouchers = dailyReceipts.stream()
                                .map(r -> SettlementVoucher.builder()
                                                .voucherType(VoucherType.SALES)
                                                .amount(r.getFinalAmount())
                                                .description(r.getSettlementDate() + " 일별 정산 합계 (가집계 내역)")
                                                .occurredAt(r.getSettlementDate().atStartOfDay())
                                                .build())
                                .collect(Collectors.toList());

                // 4. PDF 생성
                byte[] pdfBytes = fileService.createMonthlyReceiptPdf(provisionalSettlement, provisionalVouchers);

                // 5. MinIO 업로드
                String fileName = "settlement/provisional/FR_" + franchiseId + "_" + month + "_Preview_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 6. DB에 메타데이터 저장 (추적용)
                documentService.save(SettlementDocument.builder()
                                .periodType(PeriodType.MONTHLY)
                                .documentType(DocumentType.PROVISIONAL_RECEIPT_PDF)
                                .documentOwner(DocumentOwner.FRANCHISE)
                                .franchiseId(franchiseId)
                                .storageProvider("MINIO")
                                .bucket(BucketName.SETTLEMENTS.getBucketName())
                                .objectKey(fileName)
                                .fileUrl(fileUrl)
                                .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                .contentType("application/pdf")
                                .fileSize((long) pdfBytes.length)
                                .build());

                return fileUrl;
        }

        private String generateProvisionalMonthlyExcel(Long franchiseId, YearMonth month) {
                // 1. 해당 월의 모든 일별 정산 데이터 조회
                List<DailySettlementReceipt> dailyReceipts = dailyService.getAllByFranchiseAndDateRange(
                                franchiseId, month.atDay(1), month.atEndOfMonth());

                if (dailyReceipts.isEmpty()) {
                        // 상황 2: 정산 데이터 자체가 없음
                        throw new SettlementException(SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                }

                // 2. 일별 데이터를 기반으로 가상 전표 생성
                List<SettlementVoucher> provisionalVouchers = dailyReceipts.stream()
                                .map(r -> SettlementVoucher.builder()
                                                .voucherType(VoucherType.SALES)
                                                .amount(r.getFinalAmount())
                                                .description(r.getSettlementDate() + " 일별 정산 합계 (가집계 내역)")
                                                .occurredAt(r.getSettlementDate().atStartOfDay())
                                                .build())
                                .collect(Collectors.toList());

                // 3. 엑셀 생성
                byte[] excelBytes = fileService.createMonthlyVoucherExcel(provisionalVouchers);

                // 4. MinIO 업로드
                String fileName = "settlement/provisional/FR_" + franchiseId + "_" + month + "_Voucher_Preview_"
                                + System.currentTimeMillis() + ".xlsx";
                minioService.uploadFile(excelBytes, fileName,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 5. DB에 메타데이터 저장 (추적용)
                documentService.save(SettlementDocument.builder()
                                .periodType(PeriodType.MONTHLY)
                                .documentType(DocumentType.PROVISIONAL_VOUCHER_EXCEL)
                                .documentOwner(DocumentOwner.FRANCHISE)
                                .franchiseId(franchiseId)
                                .storageProvider("MINIO")
                                .bucket(BucketName.SETTLEMENTS.getBucketName())
                                .objectKey(fileName)
                                .fileUrl(fileUrl)
                                .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .fileSize((long) excelBytes.length)
                                .build());

                return fileUrl;
        }

        private MonthlySettlement aggregateMonthlySettlement(Long franchiseId, YearMonth month,
                        List<DailySettlementReceipt> dailyReceipts) {
                BigDecimal totalSale = dailyReceipts.stream().map(DailySettlementReceipt::getTotalSaleAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal orderAmount = dailyReceipts.stream().map(DailySettlementReceipt::getOrderAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal commissionFee = dailyReceipts.stream().map(DailySettlementReceipt::getCommissionFee)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal deliveryFee = dailyReceipts.stream().map(DailySettlementReceipt::getDeliveryFee)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lossAmount = dailyReceipts.stream().map(DailySettlementReceipt::getLossAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal refundAmount = dailyReceipts.stream().map(DailySettlementReceipt::getRefundAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal adjustmentAmount = dailyReceipts.stream().map(DailySettlementReceipt::getAdjustmentAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal finalAmount = dailyReceipts.stream().map(DailySettlementReceipt::getFinalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return MonthlySettlement.builder()
                                .franchiseId(franchiseId)
                                .settlementMonth(month)
                                .totalSaleAmount(totalSale)
                                .orderAmount(orderAmount)
                                .commissionFee(commissionFee)
                                .deliveryFee(deliveryFee)
                                .lossAmount(lossAmount)
                                .refundAmount(refundAmount)
                                .adjustmentAmount(adjustmentAmount)
                                .finalSettlementAmount(finalAmount)
                                .status(SettlementStatus.DRAFT) // 가집계용 상태
                                .build();
        }

        private DailySettlementReceipt aggregateDailySettlement(Long franchiseId, LocalDate date) {
                // 1. 해당 날짜의 판매 내역 조회 및 집계 (취소 안 된 것만)
                log.info("[DEBUG] 정산 조회 franchiseId={}, date={}", franchiseId, date);

                // SalesItem 기준으로 집계하여 판매 관리 페이지와 100% 일치 보장
                // 시간대 오차 방지를 위해 toLocalDate()로 직접 비교
                List<SalesItem> salesItems = salesItemRepository.findAllBySalesFranchiseId(franchiseId).stream()
                                .filter(item -> {
                                        LocalDateTime createdAt = item.getCreatedAt();
                                        return createdAt != null && createdAt.toLocalDate().equals(date);
                                })
                                .filter(item -> {
                                        Boolean canceled = item.getSales().getIsCanceled();
                                        return canceled == null || !canceled;
                                })
                                .toList();

                log.info("[DEBUG] aggregateDailySettlement - found items count: {}", salesItems.size());

                BigDecimal totalSale = salesItems.stream()
                                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // TODO: 실제 정산 로직에 맞게 수수료, 발주금액, 배송비 등 추가 집계 필요
                // 현재는 요약 페이지에서 보여주는 핵심 항목 위주로 가집계

                // 2. 해당 날짜의 발주 내역 (PENDING 포함 모든 유효 단계) 조회 및 집계
                List<FranchiseOrderStatus> validOrderStatuses = List.of(
                                FranchiseOrderStatus.PENDING, // 발주 즉시 반영을 위해 추가
                                FranchiseOrderStatus.ACCEPTED,
                                FranchiseOrderStatus.PARTIAL,
                                FranchiseOrderStatus.SHIPPING_PENDING,
                                FranchiseOrderStatus.SHIPPING,
                                FranchiseOrderStatus.COMPLETED);

                // DB 레벨 조회가 아닌, 전체 데이터를 가져와 toLocalDate()로 필터링하여 정합성 극대화
                List<FranchiseOrder> orders = orderRepository.findAllByFranchiseId(franchiseId).stream()
                                .filter(order -> {
                                        LocalDateTime createdAt = order.getCreatedAt();
                                        return createdAt != null && createdAt.toLocalDate().equals(date);
                                })
                                .filter(order -> validOrderStatuses.contains(order.getOrderStatus()))
                                .toList();

                log.info("[DEBUG] 발주대금 조회 - franchiseId={}, date={}, 전체건수={}, 필터후건수={}",
                                franchiseId, date,
                                orderRepository.findAllByFranchiseId(franchiseId).size(),
                                orders.size());

                BigDecimal orderAmount = orders.stream()
                                .map(FranchiseOrder::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. 해당 날짜의 반품 내역 (PENDING 포함 모든 유효 단계) 조회 및 집계
                List<ReturnStatus> validReturnStatuses = List.of(
                                ReturnStatus.PENDING, // 반품 신청 즉시 반영을 위해 추가
                                ReturnStatus.ACCEPTED,
                                ReturnStatus.SHIPPING_PENDING,
                                ReturnStatus.SHIPPING,
                                ReturnStatus.COMPLETED,
                                ReturnStatus.INSPECTING,
                                ReturnStatus.DEDUCTION_COMPLETED);

                // DB 레벨 조회가 아닌, 전체 데이터를 가져와 toLocalDate()로 필터링
                List<Returns> returnsList = returnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)
                                .stream()
                                .filter(ret -> {
                                        LocalDateTime createdAt = ret.getCreatedAt();
                                        return createdAt != null && createdAt.toLocalDate().equals(date);
                                })
                                .filter(ret -> validReturnStatuses.contains(ret.getReturnStatus()))
                                .toList();
                log.info("[DEBUG] 반품 조회 결과 - 건수: {}", returnsList.size());

                BigDecimal refundAmount = BigDecimal.ZERO;
                BigDecimal lossAmount = BigDecimal.ZERO;

                if (!returnsList.isEmpty()) {
                        List<Long> returnIds = returnsList.stream().map(Returns::getReturnId).toList();
                        List<ReturnItem> returnItems = returnItemRepository
                                        .findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds);

                        // 각 반품 건별로 아이템들을 단가 합산하여 집계
                        Map<Long, List<ReturnItem>> itemsByReturnId = returnItems.stream()
                                        .collect(Collectors.groupingBy(item -> item.getReturns().getReturnId()));

                        for (Returns ret : returnsList) {
                                List<ReturnItem> items = itemsByReturnId.getOrDefault(ret.getReturnId(), List.of());

                                // 품목별 단가 합산 (반품 관리 UI와 동일한 방식)
                                BigDecimal returnTotal = BigDecimal.ZERO;
                                for (ReturnItem item : items) {
                                        // 연결된 발주 품목의 단가 정보를 가져옴
                                        FranchiseOrderItem orderItem = orderItemRepository
                                                        .findById(item.getFranchiseOrderItemId()).orElse(null);
                                        if (orderItem != null) {
                                                returnTotal = returnTotal.add(orderItem.getUnitPrice());
                                        }
                                }

                                log.info("[DEBUG] 반품 건 상세 - 코드: {}, 유형: {}, 계산된 금액: {}, 상태: {}",
                                                ret.getReturnCode(), ret.getReturnType(), returnTotal,
                                                ret.getReturnStatus());

                                if (ret.getReturnType() == ReturnType.PRODUCT_DEFECT) {
                                        refundAmount = refundAmount.add(returnTotal);
                                } else if (ret.getReturnType() == ReturnType.MISORDER) {
                                        lossAmount = lossAmount.add(returnTotal);
                                }
                        }
                }

                log.info("[DEBUG] 최종 집계 결과 - 반품환급액(DEFECT): {}, 손실액(MISORDER): {}", refundAmount, lossAmount);

                // 3.3% 수수료 산출 (정수 자리로 반올림하여 계산의 명확성 확보)
                BigDecimal commissionFee = totalSale.multiply(new BigDecimal("0.033"))
                                .setScale(0, java.math.RoundingMode.HALF_UP);

                log.info("[DEBUG] 수수료 계산 - 매출: {}, 요율: 3.3%, 산출수수료: {}", totalSale, commissionFee);

                // 간소화된 가집계 결과 반환
                return DailySettlementReceipt.builder()
                                .franchiseId(franchiseId)
                                .settlementDate(date)
                                .totalSaleAmount(totalSale)
                                .orderAmount(orderAmount)
                                .refundAmount(refundAmount) // 상품하자 환급액
                                .lossAmount(lossAmount) // 오발주 손실액
                                .deliveryFee(BigDecimal.ZERO)
                                .commissionFee(commissionFee)
                                .adjustmentAmount(BigDecimal.ZERO)
                                .finalAmount(totalSale.subtract(orderAmount.add(lossAmount).add(commissionFee))
                                                .add(refundAmount))
                                .build();
        }

        private String generateProvisionalDailyPdf(Long franchiseId, LocalDate date) {
                // 1. 실시간 가집계 데이터 생성
                DailySettlementReceipt provisionalReceipt = aggregateDailySettlement(franchiseId, date);

                if (provisionalReceipt.getTotalSaleAmount().compareTo(BigDecimal.ZERO) == 0 &&
                                provisionalReceipt.getOrderAmount().compareTo(BigDecimal.ZERO) == 0) {
                        // 상황 2: 정산 데이터 자체가 없음
                        throw new SettlementException(SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                }

                // 2. 가상 전표(Line) 생성 (요약 정보만 표시)
                List<DailyReceiptLine> provisionalLines = new ArrayList<>();
                provisionalLines.add(DailyReceiptLine.builder()
                                .lineType(VoucherType.SALES)
                                .amount(provisionalReceipt.getTotalSaleAmount())
                                .description(date + " 실시간 총 매출 (가집계)")
                                .occurredAt(date.atStartOfDay())
                                .build());
                provisionalLines.add(DailyReceiptLine.builder()
                                .lineType(VoucherType.ORDER)
                                .amount(provisionalReceipt.getOrderAmount())
                                .description(date + " 실시간 총 발주 (가집계)")
                                .occurredAt(date.atStartOfDay())
                                .build());

                // 3. PDF 생성
                byte[] pdfBytes = fileService.createDailyReceiptPdf(provisionalReceipt, provisionalLines);

                // 4. MinIO 업로드 (임시 폴더)
                String fileName = "settlement/provisional/FR_" + franchiseId + "_Daily_Preview_" + date + "_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 5. DB에 메타데이터 저장 (추적용)
                documentService.save(SettlementDocument.builder()
                                .periodType(PeriodType.DAILY)
                                .documentType(DocumentType.PROVISIONAL_RECEIPT_PDF)
                                .documentOwner(DocumentOwner.FRANCHISE)
                                .storageProvider("MINIO")
                                .bucket(BucketName.SETTLEMENTS.getBucketName())
                                .franchiseId(franchiseId)
                                .objectKey(fileName)
                                .fileUrl(fileUrl)
                                .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                .contentType("application/pdf")
                                .fileSize((long) pdfBytes.length)
                                .build());

                return fileUrl;
        }
}
