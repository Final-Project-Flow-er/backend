package com.chaing.api.facade.franchise;


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
import com.chaing.domain.businessunits.repository.FranchiseRepository;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.settlements.service.SettlementDocumentService;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.SettlementVoucherRepository;
import com.chaing.domain.settlements.entity.SettlementVoucher;
import com.chaing.domain.products.entity.Product;
import com.chaing.domain.products.repository.ProductRepository;
import com.chaing.domain.settlements.entity.SettlementDocument;
import com.chaing.domain.settlements.service.SettlementFileService;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.MinioService;
import com.chaing.domain.transports.dto.DeliveryFeeInfo;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.repository.TransitRepository;
import com.chaing.domain.transports.service.InternalTransportService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalTime;
import com.chaing.api.dto.franchise.settlement.response.FranchiseDailyGraphResponse;

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
        private final ProductRepository productRepository;

        // 배송 정보 조회를 위한 Repository 및 Service 주입
        private final TransitRepository transitRepository;
        private final InternalTransportService transportService;
        private final FranchiseRepository franchiseRepository;

        // 일별 정산 요약
        @Transactional(readOnly = true)
        public FranchiseSettlementSummaryResponse getDailySummary(Long franchiseId, LocalDate date) {
                DailySettlementReceipt receipt = getInternalDailyAggregation(franchiseId, date).receipt();
                String name = franchiseRepository.findById(franchiseId).map(Franchise::getName).orElse("Unknown");
                return toSummary(name, receipt);
        }

        // 내부 합산용 일별 정산 조회 (실시간 또는 DB)
        private AggregationResult getInternalDailyAggregation(Long franchiseId, LocalDate date) {
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
                        return aggregateDailySettlement(franchiseId, date);
                }

                // DB 기록이 있으면 해당 기록과 연관된 전표 라인들을 로드하여 반환
                return new AggregationResult(receipt, dailyService.getAllReceiptLines(receipt.getDailyReceiptId()));
        }

        // 일별 매출 top5, 전체
        @Transactional(readOnly = true)
        public List<FranchiseSalesItemResponse> getDailySalesItems(
                        Long franchiseId, LocalDate date, Integer limit) {
                log.info("[DEBUG] getDailySalesItems - franchiseId: {}, date: {}",
                                franchiseId, date);

                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                // DB 레벨 필터링 사용
                List<SalesItem> items = salesItemRepository.findAllBySalesFranchiseIdAndCreatedAtBetween(franchiseId, start, end)
                                .stream()
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
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);
                List<FranchiseOrder> orders = orderRepository.findAllByFranchiseIdAndOrderStatusInAndCreatedAtBetween(
                                franchiseId, validStatuses, start, end);
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
                java.util.Optional<MonthlySettlement> settlementOpt = monthlyService.findByFranchiseAndMonth(
                                franchiseId,
                                month);

                if (settlementOpt.isPresent()) {
                        MonthlySettlement s = settlementOpt.get();
                        String name = franchiseRepository.findById(franchiseId).map(Franchise::getName).orElse("Unknown");
                        return new FranchiseSettlementSummaryResponse(
                                        name,
                                        s.getFinalSettlementAmount(),
                                        s.getTotalSaleAmount(),
                                        s.getRefundAmount(),
                                        s.getOrderAmount(),
                                        s.getDeliveryFee(),
                                        s.getLossAmount(),
                                        s.getCommissionFee(),
                                        s.getAdjustmentAmount());
                }

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
                                dailyReceipts.add(getInternalDailyAggregation(franchiseId, date).receipt());
                        } catch (Exception ex) {
                                log.warn("[DEBUG] Skip aggregation for date {} due to: {}", date,
                                                ex.getMessage());
                        }
                }

                if (dailyReceipts.isEmpty()) {
                        // 데이터가 아예 없는 경우 기본 응답 (0원) 반환하거나 예외 처리
                        String name = franchiseRepository.findById(franchiseId).map(Franchise::getName).orElse("Unknown");
                        return new FranchiseSettlementSummaryResponse(
                                        name,
                                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                        BigDecimal.ZERO, BigDecimal.ZERO);
                }

                String name = franchiseRepository.findById(franchiseId).map(Franchise::getName).orElse("Unknown");
                return toSummary(name, aggregateMonthlySettlement(franchiseId, month, dailyReceipts));
        }

        // 월별 매출 현황 top5, 전체
        @Transactional(readOnly = true)
        public List<FranchiseSalesItemResponse> getMonthlySalesItems(
                        Long franchiseId, YearMonth month, Integer limit) {
                LocalDateTime start = month.atDay(1).atStartOfDay();
                LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);

                // DB 레벨 필터링 사용
                List<SalesItem> items = salesItemRepository.findAllBySalesFranchiseIdAndCreatedAtBetween(franchiseId, start, end)
                                .stream()
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
                                                end.atTime(LocalTime.MAX));
                List<Long> orderIds = orders.stream()
                                .map(FranchiseOrder::getFranchiseOrderId).toList();
                List<FranchiseOrderItem> items = orderItemRepository
                                .findAllByFranchiseOrderFranchiseOrderIdIn(orderIds);
                return aggregateOrderItems(items, limit);
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
                        // MONTHLY → 당월인 경우 실시간 합산, 과거인 경우 SettlementVoucher 조회
                        YearMonth currentMonth = YearMonth.now();
                        if (month.equals(currentMonth)) {
                                log.info("[DEBUG] getVouchers (MONTHLY) - Current month detected. Aggregating range-based vouchers.");
                                LocalDate start = month.atDay(1);
                                LocalDate end = LocalDate.now();

                                List<FranchiseVoucherResponse> allVouchers = aggregateVouchersInRange(
                                                franchiseId, start, end, type);

                                // 최신순 정렬
                                allVouchers.sort((a, b) -> {
                                        if (a.occurredAt() == null || b.occurredAt() == null)
                                                return 0;
                                        return b.occurredAt().compareTo(a.occurredAt());
                                });

                                // 페이징
                                int startIdx = (int) pageable.getOffset();
                                int endIdx = Math.min((startIdx + pageable.getPageSize()), allVouchers.size());

                                if (startIdx >= allVouchers.size()) {
                                        return new org.springframework.data.domain.PageImpl<>(new ArrayList<>(),
                                                        pageable,
                                                        allVouchers.size());
                                }
                                return new org.springframework.data.domain.PageImpl<>(
                                                allVouchers.subList(startIdx, endIdx), pageable, allVouchers.size());
                        }

                        // 과거 데이터 (DB 조회)
                        java.util.Optional<MonthlySettlement> settlementOpt = monthlyService.findByFranchiseAndMonth(
                                        franchiseId,
                                        month);
                        if (settlementOpt.isEmpty()) {
                                return Page.empty(pageable);
                        }
                        MonthlySettlement settlement = settlementOpt.get();
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

        /**
         * 특정 기간 동안의 전표 내역을 통합 집계함 (실시간)
         */
        private List<FranchiseVoucherResponse> aggregateVouchersInRange(
                        Long franchiseId, LocalDate start, LocalDate end, VoucherType filterType) {
                List<FranchiseVoucherResponse> vouchers = new ArrayList<>();

                // 1. 매출 (SALES)
                if (filterType == null || filterType == VoucherType.SALES) {
                        List<SalesItem> salesItems = salesItemRepository.findAllBySalesFranchiseIdAndCreatedAtBetween(
                                        franchiseId, start.atStartOfDay(), end.atTime(LocalTime.MAX))
                                        .stream()
                                        .filter(item -> {
                                                Boolean canceled = item.getSales().getIsCanceled();
                                                return canceled == null || !canceled;
                                        })
                                        .toList();
                        for (SalesItem item : salesItems) {
                                vouchers.add(new FranchiseVoucherResponse(
                                                item.getSales().getSalesCode(),
                                                VoucherType.SALES,
                                                item.getProductName() + " (판매)",
                                                item.getQuantity(),
                                                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                                item.getCreatedAt()));
                        }
                }

                // 2. 발주 (ORDER)
                if (filterType == null || filterType == VoucherType.ORDER) {
                        List<FranchiseOrderStatus> validStatuses = List.of(
                                        FranchiseOrderStatus.PENDING, FranchiseOrderStatus.ACCEPTED,
                                        FranchiseOrderStatus.PARTIAL, FranchiseOrderStatus.SHIPPING_PENDING,
                                        FranchiseOrderStatus.SHIPPING, FranchiseOrderStatus.COMPLETED);

                        List<FranchiseOrder> orders = orderRepository.findAllByFranchiseIdAndOrderStatusInAndCreatedAtBetween(
                                        franchiseId, validStatuses, start.atStartOfDay(), end.atTime(LocalTime.MAX));

                        if (!orders.isEmpty()) {
                                List<Long> orderIds = orders.stream().map(FranchiseOrder::getFranchiseOrderId).toList();
                                List<FranchiseOrderItem> orderItems = orderItemRepository
                                                .findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);

                                List<Long> productIds = orderItems.stream().map(FranchiseOrderItem::getProductId)
                                                .distinct().toList();
                                Map<Long, String> productNames = productRepository.findAllByProductIdIn(productIds)
                                                .stream()
                                                .collect(Collectors.toMap(Product::getProductId, Product::getName));

                                for (FranchiseOrderItem item : orderItems) {
                                        String name = productNames.getOrDefault(item.getProductId(), "알 수 없는 상품");
                                        vouchers.add(new FranchiseVoucherResponse(
                                                        item.getFranchiseOrder().getOrderCode(),
                                                        VoucherType.ORDER,
                                                        name + " (발주)",
                                                        item.getQuantity(),
                                                        item.getTotalPrice(),
                                                        item.getCreatedAt()));
                                }
                        }
                }

                // 3. 반품 (REFUND / LOSS)
                if (filterType == null || filterType == VoucherType.REFUND || filterType == VoucherType.LOSS) {
                        List<ReturnStatus> validReturnStatuses = List.of(
                                        ReturnStatus.PENDING, ReturnStatus.ACCEPTED, ReturnStatus.SHIPPING_PENDING,
                                        ReturnStatus.SHIPPING, ReturnStatus.COMPLETED, ReturnStatus.INSPECTING,
                                        ReturnStatus.DEDUCTION_COMPLETED);

                        List<Returns> returnsList = returnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)
                                        .stream()
                                        .filter(ret -> {
                                                LocalDateTime createdAt = ret.getCreatedAt();
                                                if (createdAt == null)
                                                        return false;
                                                LocalDate d = createdAt.toLocalDate();
                                                return !d.isBefore(start) && !d.isAfter(end);
                                        })
                                        .filter(ret -> validReturnStatuses.contains(ret.getReturnStatus()))
                                        .toList();

                        for (Returns ret : returnsList) {
                                List<ReturnItem> items = returnItemRepository
                                                .findAllByReturns_ReturnIdInAndDeletedAtIsNull(
                                                                List.of(ret.getReturnId()));
                                BigDecimal returnTotal = BigDecimal.ZERO;
                                for (ReturnItem item : items) {
                                        FranchiseOrderItem orderItem = orderItemRepository
                                                        .findById(item.getFranchiseOrderItemId()).orElse(null);
                                        if (orderItem != null)
                                                returnTotal = returnTotal.add(orderItem.getUnitPrice());
                                }

                                VoucherType type = (ret.getReturnType() == ReturnType.PRODUCT_DEFECT)
                                                ? VoucherType.REFUND
                                                : VoucherType.LOSS;
                                if (filterType == null || filterType == type) {
                                        vouchers.add(new FranchiseVoucherResponse(
                                                        ret.getReturnCode(),
                                                        type,
                                                        ret.getReturnCode() + (type == VoucherType.REFUND ? " (상품하자 반품)"
                                                                        : " (오발주 손실)"),
                                                        null,
                                                        returnTotal,
                                                        ret.getCreatedAt()));
                                }
                        }
                }

                // 4. 배송비 (DELIVERY)
                if (filterType == null || filterType == VoucherType.DELIVERY) {
                        List<DeliverStatus> validDeliverStatuses = List.of(DeliverStatus.IN_TRANSIT,
                                        DeliverStatus.DELIVERED);
                        List<Transit> transits = transitRepository.findAllByFranchiseIdAndStatusInAndCreatedAtBetween(
                                        franchiseId, validDeliverStatuses, start.atStartOfDay(), end.atTime(LocalTime.MAX));

                        Map<String, List<Transit>> transitsByTracking = transits.stream()
                                        .collect(Collectors.groupingBy(Transit::getTrackingNumber));

                        for (Map.Entry<String, List<Transit>> entry : transitsByTracking.entrySet()) {
                                String trackingNumber = entry.getKey();
                                List<Transit> group = entry.getValue();
                                List<OrderInfo> orderInfos = group.stream()
                                                .map(t -> new OrderInfo(null, t.getOrderCode(), t.getWeight(),
                                                                t.getFranchiseId(), null, null))
                                                .toList();
                                Long vehicleId = group.get(0).getVehicleId();
                                List<DeliveryFeeInfo> feeInfos = transportService.calculateDeliveryFee(orderInfos,
                                                vehicleId);

                                for (DeliveryFeeInfo feeInfo : feeInfos) {
                                        if (feeInfo.franchiseId().equals(franchiseId)) {
                                                vouchers.add(new FranchiseVoucherResponse(
                                                                trackingNumber,
                                                                VoucherType.DELIVERY,
                                                                "배송비 (송장: " + trackingNumber + ")",
                                                                null,
                                                                feeInfo.deliveryFee(),
                                                                group.get(0).getCreatedAt()));
                                        }
                                }
                        }
                }

                // 5. 수수료 (COMMISSION) - 일단 매출이 있는 날짜별로 합산하여 표시
                if (filterType == null || filterType == VoucherType.COMMISSION) {
                        // 일자별 매출 합산
                        Map<LocalDate, BigDecimal> salesByDate = salesItemRepository
                                        .findAllBySalesFranchiseIdAndCreatedAtBetween(franchiseId, start.atStartOfDay(), end.atTime(LocalTime.MAX))
                                        .stream()
                                        .filter(item -> {
                                                Boolean canceled = item.getSales().getIsCanceled();
                                                return canceled == null || !canceled;
                                        })
                                        .collect(Collectors.groupingBy(item -> item.getCreatedAt().toLocalDate(),
                                                        Collectors.reducing(BigDecimal.ZERO,
                                                                        item -> item.getUnitPrice().multiply(BigDecimal
                                                                                        .valueOf(item.getQuantity())),
                                                                        BigDecimal::add)));

                        salesByDate.forEach((date, total) -> {
                                BigDecimal commission = total.multiply(new BigDecimal("0.033")).setScale(0,
                                                java.math.RoundingMode.HALF_UP);
                                if (commission.compareTo(BigDecimal.ZERO) > 0) {
                                        vouchers.add(new FranchiseVoucherResponse(
                                                        "COMM-" + date.toString().replace("-", ""),
                                                        VoucherType.COMMISSION,
                                                        "판매 수수료 (3.3%) - " + date,
                                                        null,
                                                        commission,
                                                        date.atTime(23, 59, 59)));
                                }
                        });
                }

                return vouchers;
        }

        // 내부 메서드, 외부 controller에서 직접 부를 수 없는 private로 선언.
        // domain으로 보내지 않고 api모듈에서 계속 사용

        // 일별 정산 요약 데이터 받기
        private FranchiseSettlementSummaryResponse toSummary(String franchiseName, DailySettlementReceipt r) {
                return new FranchiseSettlementSummaryResponse(
                                franchiseName,
                                r.getFinalAmount(),
                                r.getTotalSaleAmount(),
                                r.getRefundAmount(),
                                r.getOrderAmount(),
                                r.getDeliveryFee(),
                                r.getLossAmount(),
                                r.getCommissionFee(),
                                r.getAdjustmentAmount());
        }

        private FranchiseSettlementSummaryResponse toSummary(String franchiseName, MonthlySettlement s) {
                return new FranchiseSettlementSummaryResponse(
                                franchiseName,
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
                log.info("[DEBUG] Generating real-time vouchers for franchiseId: {}, date: {}, filter: {}",
                                franchiseId, date, filterType);

                // 상위 집계 메서드 재사용 (데이터 일관성 확보)
                AggregationResult result = aggregateDailySettlement(franchiseId, date);

                List<FranchiseVoucherResponse> allVouchers = result.lines().stream()
                                .filter(line -> filterType == null || line.getLineType() == filterType)
                                .map(this::toVoucherResponse)
                                .collect(Collectors.toList());

                // 발생시간 역순 정렬 (최신순)
                allVouchers.sort((a, b) -> {
                        if (a.occurredAt() == null || b.occurredAt() == null)
                                return 0;
                        return b.occurredAt().compareTo(a.occurredAt());
                });

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

        // OrderItem 집계
        // 발주 상품별로 묶고 더해서 순위 매기기
        private List<FranchiseOrderItemResponse> aggregateOrderItems(
                        List<FranchiseOrderItem> items, Integer limit) {
                // productId별 그룹핑
                Map<Long, List<FranchiseOrderItem>> grouped = items.stream()
                                .collect(Collectors.groupingBy(FranchiseOrderItem::getProductId));

                // 여러 productId에 대한 상품 정보를 한 번에 조회
                List<Long> productIds = new ArrayList<>(grouped.keySet());
                Map<Long, String> productNames = productRepository.findAllByProductIdIn(productIds).stream()
                                .collect(Collectors.toMap(Product::getProductId, Product::getName));

                List<FranchiseOrderItemResponse> result = grouped.entrySet().stream()
                                .map(entry -> {
                                        Long productId = entry.getKey();
                                        String productName = productNames.getOrDefault(productId,
                                                        "알 수 없는 상품 (ID: " + productId + ")");
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
                String uploadedFileName = null; // 롤백 시 삭제할 파일명 추적
                try {
                        // 1. DB에서 확정 정산 레코드 조회
                        java.util.Optional<DailySettlementReceipt> receiptOpt = dailyService
                                        .findByFranchiseAndDate(franchiseId, date);

                        // 2. 기존 문서 레코드가 있으면 삭제 (매 요청마다 최신 데이터로 재생성)
                        if (receiptOpt.isPresent()) {
                                java.util.Optional<SettlementDocument> existingDoc = documentService
                                                .getDailyDocument(receiptOpt.get().getDailyReceiptId());
                                existingDoc.ifPresent(doc -> {
                                        documentService.deleteById(doc.getSettlementDocumentId());
                                });
                        }

                        // 3. 현재 정산 데이터 실시간 집계
                        AggregationResult result = aggregateDailySettlement(franchiseId, date);
                        DailySettlementReceipt currentReceipt = result.receipt();
                        List<DailyReceiptLine> currentLines = result.lines();

                        /* 데이터가 전혀 없는 경우에도 PDF 생성을 허용하도록 예외 처리 주석 처리
                        if (currentReceipt.getTotalSaleAmount().compareTo(BigDecimal.ZERO) == 0 &&
                                        currentReceipt.getOrderAmount().compareTo(BigDecimal.ZERO) == 0) {
                                throw new SettlementException(SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                        }
                        */

                        // [중요] ID missing 및 중복 키 에러 해결: 기존 데이터 조회 후 Upsert
                        java.util.Optional<DailySettlementReceipt> existingReceipt = 
                            dailyService.findByFranchiseAndDate(franchiseId, date);

                        if (existingReceipt.isPresent()) {
                            DailySettlementReceipt legacy = existingReceipt.get();
                            legacy.updateAmounts(
                                currentReceipt.getTotalSaleAmount(),
                                currentReceipt.getOrderAmount(),
                                currentReceipt.getDeliveryFee(),
                                currentReceipt.getCommissionFee(),
                                currentReceipt.getLossAmount(),
                                currentReceipt.getRefundAmount(),
                                currentReceipt.getAdjustmentAmount(),
                                currentReceipt.getFinalAmount()
                            );
                            currentReceipt = dailyService.save(legacy);
                        } else {
                            currentReceipt = dailyService.save(currentReceipt);
                        }


                        // 4. 가맹점명 조회
                        String franchiseName = franchiseRepository.findById(franchiseId)
                                .map(com.chaing.domain.businessunits.entity.Franchise::getName)
                                .orElse("Unknown Store");

                        // 5. PDF 생성
                        byte[] pdfBytes = fileService.createDailyReceiptPdf(currentReceipt, currentLines, franchiseName);

                        String fileName = "settlement/daily/FR_" + franchiseId + "_Daily_" + date + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        uploadedFileName = fileName;

                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        SettlementDocument.SettlementDocumentBuilder docBuilder = SettlementDocument.builder()
                                        .periodType(PeriodType.DAILY)
                                        .documentType(DocumentType.RECEIPT_PDF)
                                        .documentOwner(DocumentOwner.FRANCHISE)
                                        .franchiseId(franchiseId)
                                        .dailyReceiptId(currentReceipt.getDailyReceiptId()) // 영속화된 ID 사용
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/pdf")
                                        .fileSize((long) pdfBytes.length);

                        documentService.save(docBuilder.build());
                        return fileUrl;
                } catch (Exception e) {
                        log.error("[ERROR] Daily PDF generation failed for franchise: {}, date: {}. Error: {}", 
                                franchiseId, date, e.getMessage());
                        
                        // 업로드 후 DB 저장 실패 시 MinIO 고아 파일 삭제
                        if (uploadedFileName != null) {
                                try {
                                        minioService.deleteFile(uploadedFileName, BucketName.SETTLEMENTS);
                                } catch (Exception ex) {
                                        log.warn("[ROLLBACK ERROR] Failed to delete orphan file: {}", uploadedFileName);
                                }
                        }

                        if (e instanceof SettlementException) {
                                throw e;
                        }
                        throw new SettlementException(SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }


        @Transactional
        public String getMonthlyReceiptPdf(Long franchiseId, YearMonth month) {
                try {
                        // 1. DB에서 확정 월별 정산 조회
                        java.util.Optional<MonthlySettlement> settlementOpt = monthlyService
                                        .findByFranchiseAndMonth(franchiseId, month);

                        String franchiseName = franchiseRepository.findById(franchiseId)
                                        .map(Franchise::getName).orElse("Unknown Store");

                        byte[] pdfBytes;
                        String fileName = "settlement/monthly/FR_" + franchiseId + "_Monthly_"
                                        + month + "_" + System.currentTimeMillis() + ".pdf";

                        if (settlementOpt.isPresent()) {
                                MonthlySettlement settlement = settlementOpt.get();

                                // 2. 기존 문서 레코드 삭제 (매 요청마다 최신 데이터로 재생성)
                                List<SettlementDocument> documents = documentService
                                                .getMonthlyDocuments(settlement.getMonthlySettlementId());
                                documents.stream()
                                                .filter(doc -> doc.getDocumentType() == DocumentType.RECEIPT_PDF)
                                                .forEach(doc -> documentService.deleteById(doc.getSettlementDocumentId()));

                                // 3. 확정 스냅샷으로 RECEIPT_PDF 생성
                                List<SettlementVoucher> vouchers = voucherRepository
                                                .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());
                                pdfBytes = fileService.createMonthlyReceiptPdf(settlement, vouchers, franchiseName);

                                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                                documentService.save(SettlementDocument.builder()
                                                .periodType(PeriodType.MONTHLY)
                                                .documentType(DocumentType.RECEIPT_PDF)
                                                .documentOwner(DocumentOwner.FRANCHISE)
                                                .franchiseId(franchiseId)
                                                .monthlySettlementId(settlement.getMonthlySettlementId())
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

                        // 4. 확정 레코드 없으면 실시간 가집계로 PDF 생성 (당월 등)
                        return generateProvisionalMonthlyPdf(franchiseId, month);
                } catch (Exception e) {
                        log.error("Failed to generate Monthly Franchise Receipt PDF: ", e);
                        if (e instanceof SettlementException)
                                throw (SettlementException) e;
                        throw new SettlementException(SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getMonthlyVouchersExcel(Long franchiseId, YearMonth month) {
                try {
                        // 1. 해당 가맹점의 월별 정산 데이터 조회 (Optional 사용으로 트랜잭션 롤백 방지)
                        java.util.Optional<MonthlySettlement> settlementOpt = monthlyService
                                        .findByFranchiseAndMonth(franchiseId, month);

                        if (settlementOpt.isPresent()) {
                                MonthlySettlement settlement = settlementOpt.get();
                                // 2. 이미 생성된 엑셀 문서가 있는지 확인
                                List<SettlementDocument> documents = documentService
                                                .getMonthlyDocuments(settlement.getMonthlySettlementId());
                                if (documents != null) {
                                        var existingExcel = documents.stream()
                                                        .filter(doc -> doc
                                                                        .getDocumentType() == DocumentType.VOUCHER_EXCEL)
                                                        .findFirst();
                                        if (existingExcel.isPresent()) {
                                                return minioService.getFileUrl(existingExcel.get().getObjectKey(),
                                                                BucketName.SETTLEMENTS);
                                        }
                                }
                        }

                        // 3. 데이터가 없으면 실시간 가집계 엑셀 생성 시도
                        return generateProvisionalMonthlyExcel(franchiseId, month);
                } catch (Exception e) {
                        log.error("Failed to generate Monthly Franchise Vouchers Excel: ", e);
                        if (e instanceof SettlementException)
                                throw (SettlementException) e;
                        throw new SettlementException(SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        private String generateProvisionalMonthlyPdf(Long franchiseId, YearMonth month) {
                String uploadedFileName = null;
                try {
                        // 1. 해당 월의 모든 날짜를 순회하며 데이터 수집
                        LocalDate start = month.atDay(1);
                        LocalDate end = month.atEndOfMonth();
                        LocalDate today = LocalDate.now();
                        if (end.isAfter(today)) {
                                end = today;
                        }

                        List<DailySettlementReceipt> receipts = new ArrayList<>();
                        List<SettlementVoucher> vouchers = new ArrayList<>();

                        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                                AggregationResult result = getInternalDailyAggregation(franchiseId, date);
                                DailySettlementReceipt receipt = result.receipt();

                                if (receipt.getTotalSaleAmount().compareTo(BigDecimal.ZERO) > 0 ||
                                                receipt.getOrderAmount().compareTo(BigDecimal.ZERO) > 0 ||
                                                receipt.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {

                                        receipts.add(receipt);
                                        vouchers.add(SettlementVoucher.builder()
                                                        .voucherType(VoucherType.SALES)
                                                        .amount(receipt.getFinalAmount())
                                                        .description(date + " 일별 정산 합계")
                                                        .occurredAt(date.atStartOfDay())
                                                        .build());
                                }
                        }

                        if (receipts.isEmpty()) {
                                throw new SettlementException(SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                        }

                        // 2. 가집계 MonthlySettlement 객체 생성 및 [Upsert] (ID 확보 및 중복 방지)
                        MonthlySettlement provisionalSettlement = aggregateMonthlySettlement(franchiseId, month, receipts);
                        
                        java.util.Optional<MonthlySettlement> existingSettlement = 
                            monthlyService.findByFranchiseAndMonth(franchiseId, month);

                        if (existingSettlement.isPresent()) {
                            MonthlySettlement legacy = existingSettlement.get();
                            legacy.updateAmounts(
                                provisionalSettlement.getTotalSaleAmount(),
                                provisionalSettlement.getOrderAmount(),
                                provisionalSettlement.getDeliveryFee(),
                                provisionalSettlement.getCommissionFee(),
                                provisionalSettlement.getLossAmount(),
                                provisionalSettlement.getRefundAmount(),
                                provisionalSettlement.getAdjustmentAmount(),
                                provisionalSettlement.getFinalSettlementAmount()
                            );
                            provisionalSettlement = monthlyService.save(legacy);
                        } else {
                            provisionalSettlement = monthlyService.save(provisionalSettlement);
                        }

                        // 가맹점명 조회
                        String franchiseName = franchiseRepository.findById(franchiseId)
                                .map(com.chaing.domain.businessunits.entity.Franchise::getName)
                                .orElse("Unknown Store");

                        // 3. PDF 생성
                        byte[] pdfBytes = fileService.createMonthlyReceiptPdf(provisionalSettlement, vouchers, franchiseName);

                        // 4. MinIO 업로드
                        String fileName = "settlement/provisional/FR_" + franchiseId + "_" + month + "_Preview_"
                                        + System.currentTimeMillis() + ".pdf";
                        uploadedFileName = fileName;
                        
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        // 5. DB에 메타데이터 저장
                        documentService.save(SettlementDocument.builder()
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(DocumentType.PROVISIONAL_RECEIPT_PDF)
                                        .documentOwner(DocumentOwner.FRANCHISE)
                                        .franchiseId(franchiseId)
                                        .monthlySettlementId(provisionalSettlement.getMonthlySettlementId()) // ID 연결
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/pdf")
                                        .fileSize((long) pdfBytes.length)
                                        .build());

                        return fileUrl;
                } catch (Exception e) {
                        log.error("[ERROR] Provisional Monthly PDF generation failed: ", e);
                        if (uploadedFileName != null) {
                                try {
                                        minioService.deleteFile(uploadedFileName, BucketName.SETTLEMENTS);
                                } catch (Exception ex) {
                                        log.warn("[ROLLBACK ERROR] Failed to delete orphan file: {}", uploadedFileName);
                                }
                        }
                        throw e;
                }
        }

        private String generateProvisionalMonthlyExcel(Long franchiseId, YearMonth month) {
                String uploadedFileName = null;
                try {
                        // 1. 해당 월의 모든 날짜를 순회하며 전표 데이터 수집
                        LocalDate start = month.atDay(1);
                        LocalDate end = month.atEndOfMonth();
                        LocalDate today = LocalDate.now();
                        if (end.isAfter(today)) {
                                end = today;
                        }

                        List<DailySettlementReceipt> receipts = new ArrayList<>();
                        List<SettlementVoucher> vouchers = new ArrayList<>();

                        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                                AggregationResult result = getInternalDailyAggregation(franchiseId, date);
                                if (!result.lines().isEmpty()) {
                                        receipts.add(result.receipt());
                                        for (DailyReceiptLine line : result.lines()) {
                                                vouchers.add(SettlementVoucher.builder()
                                                                .voucherType(line.getLineType())
                                                                .amount(line.getAmount())
                                                                .description(date + ": " + line.getDescription())
                                                                .occurredAt(line.getOccurredAt())
                                                                .referenceCode(line.getReferenceCode())
                                                                .build());
                                        }
                                }
                        }

                        if (vouchers.isEmpty()) {
                                throw new SettlementException(SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                        }

                        // [중요] ID missing 및 중복 키 에러 해결을 위해 MonthlySettlement 가집계 데이터 Upsert
                        MonthlySettlement provisionalSettlement = aggregateMonthlySettlement(franchiseId, month, receipts);
                        
                        java.util.Optional<MonthlySettlement> existingSettlement = 
                            monthlyService.findByFranchiseAndMonth(franchiseId, month);

                        if (existingSettlement.isPresent()) {
                            MonthlySettlement legacy = existingSettlement.get();
                            legacy.updateAmounts(
                                provisionalSettlement.getTotalSaleAmount(),
                                provisionalSettlement.getOrderAmount(),
                                provisionalSettlement.getDeliveryFee(),
                                provisionalSettlement.getCommissionFee(),
                                provisionalSettlement.getLossAmount(),
                                provisionalSettlement.getRefundAmount(),
                                provisionalSettlement.getAdjustmentAmount(),
                                provisionalSettlement.getFinalSettlementAmount()
                            );
                            provisionalSettlement = monthlyService.save(legacy);
                        } else {
                            provisionalSettlement = monthlyService.save(provisionalSettlement);
                        }

                        // 2. 엑셀 생성
                        byte[] excelBytes = fileService.createMonthlyVoucherExcel(vouchers);

                        // 3. MinIO 업로드
                        String fileName = "settlement/provisional/FR_" + franchiseId + "_" + month + "_Voucher_Preview_"
                                        + System.currentTimeMillis() + ".xlsx";
                        uploadedFileName = fileName;

                        minioService.uploadFile(excelBytes, fileName,
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        // 4. DB에 메타데이터 저장
                        documentService.save(SettlementDocument.builder()
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(DocumentType.PROVISIONAL_VOUCHER_EXCEL)
                                        .documentOwner(DocumentOwner.FRANCHISE)
                                        .franchiseId(franchiseId)
                                        .monthlySettlementId(provisionalSettlement.getMonthlySettlementId()) // ID 연결
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                        .fileSize((long) excelBytes.length)
                                        .build());

                        return fileUrl;
                } catch (Exception e) {
                        log.error("[ERROR] Provisional Monthly Excel generation failed: ", e);
                        if (uploadedFileName != null) {
                                try {
                                        minioService.deleteFile(uploadedFileName, BucketName.SETTLEMENTS);
                                } catch (Exception ex) {
                                        log.warn("[ROLLBACK ERROR] Failed to delete orphan file: {}", uploadedFileName);
                                }
                        }
                        throw e;
                }
        }

        private MonthlySettlement aggregateMonthlySettlement(Long franchiseId, YearMonth month,
                        List<DailySettlementReceipt> dailyReceipts) {
                BigDecimal totalSale = dailyReceipts.stream()
                                .map(r -> r.getTotalSaleAmount() != null ? r.getTotalSaleAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal orderAmount = dailyReceipts.stream()
                                .map(r -> r.getOrderAmount() != null ? r.getOrderAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal commissionFee = dailyReceipts.stream()
                                .map(r -> r.getCommissionFee() != null ? r.getCommissionFee() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal deliveryFee = dailyReceipts.stream()
                                .map(r -> r.getDeliveryFee() != null ? r.getDeliveryFee() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lossAmount = dailyReceipts.stream()
                                .map(r -> r.getLossAmount() != null ? r.getLossAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal refundAmount = dailyReceipts.stream()
                                .map(r -> r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal adjustmentAmount = dailyReceipts.stream()
                                .map(r -> r.getAdjustmentAmount() != null ? r.getAdjustmentAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal finalAmount = dailyReceipts.stream()
                                .map(r -> r.getFinalAmount() != null ? r.getFinalAmount() : BigDecimal.ZERO)
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

        // 실시간 가집계 데이터 및 상세 전표 라인 수집을 위한 내부 레코드
        private record AggregationResult(
                        DailySettlementReceipt receipt,
                        List<DailyReceiptLine> lines) {
        }

        // 실시간 가집계 (매출, 수수료, 발주, 배송비, 반품 등)
        private AggregationResult aggregateDailySettlement(Long franchiseId, LocalDate date) {
                List<DailyReceiptLine> lines = new ArrayList<>();
                // 1. 해당 날짜의 판매 내역 조회 및 집계 (취소 안 된 것만)
                log.info("[DEBUG] 정산 조회 franchiseId={}, date={}", franchiseId, date);

                // SalesItem 기준으로 집계하여 판매 관리 페이지와 100% 일치 보장
                // 시간대 오차 방지를 위해 toLocalDate()로 직접 비교
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                List<SalesItem> salesItems = salesItemRepository.findAllBySalesFranchiseIdAndCreatedAtBetween(franchiseId, start, end)
                                .stream()
                                .filter(item -> {
                                        Boolean canceled = item.getSales().getIsCanceled();
                                        return canceled == null || !canceled;
                                })
                                .toList();

                log.info("[DEBUG] aggregateDailySettlement - found items count: {}", salesItems.size());

                BigDecimal totalSale = salesItems.stream()
                                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 판매 상세 라인 추가
                for (SalesItem item : salesItems) {
                        lines.add(DailyReceiptLine.builder()
                                        .lineType(VoucherType.SALES)
                                        .description(item.getProductName() + " (판매)")
                                        .productName(item.getProductName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .amount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .occurredAt(item.getCreatedAt())
                                        .referenceCode(item.getSales().getSalesCode())
                                        .build());
                }

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
                List<FranchiseOrder> orders = orderRepository.findAllByFranchiseIdAndOrderStatusInAndCreatedAtBetween(
                                franchiseId, validOrderStatuses, date.atStartOfDay(), date.atTime(LocalTime.MAX));

                log.info("[DEBUG] 발주대금 조회 - franchiseId={}, date={}, 전체건수={}, 필터후건수={}",
                                franchiseId, date,
                                orderRepository.findAllByFranchiseId(franchiseId).size(),
                                orders.size());

                BigDecimal orderAmount = BigDecimal.ZERO;
                if (!orders.isEmpty()) {
                        List<Long> orderIds = orders.stream().map(FranchiseOrder::getFranchiseOrderId).toList();
                        List<FranchiseOrderItem> orderItems = orderItemRepository
                                        .findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);

                        orderAmount = orderItems.stream()
                                        .map(FranchiseOrderItem::getTotalPrice)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        log.info("[DEBUG] 발주 품목 상세 집계 - 발주건수: {}, 전체품목수: {}, 총액: {}",
                                        orders.size(), orderItems.size(), orderAmount);

                        // 발주 상세 라인 추가 (상품명 매핑 포함)
                        // 여러 productId에 대한 상품 정보를 한 번에 조회
                        List<Long> productIds = orderItems.stream().map(FranchiseOrderItem::getProductId).distinct()
                                        .toList();
                        Map<Long, String> productNames = productRepository.findAllByProductIdIn(productIds).stream()
                                        .collect(Collectors.toMap(Product::getProductId, Product::getName));

                        for (FranchiseOrderItem item : orderItems) {
                                String name = productNames.getOrDefault(item.getProductId(),
                                                "알 수 없는 상품 (ID: " + item.getProductId() + ")");
                                lines.add(DailyReceiptLine.builder()
                                                .lineType(VoucherType.ORDER)
                                                .description(name + " (발주)")
                                                .productName(name)
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .amount(item.getTotalPrice())
                                                .occurredAt(item.getCreatedAt())
                                                .referenceCode(item.getFranchiseOrder().getOrderCode())
                                                .build());
                        }
                }

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
                                        lines.add(DailyReceiptLine.builder()
                                                        .lineType(VoucherType.REFUND)
                                                        .description(ret.getReturnCode() + " (상품하자 반품)")
                                                        .amount(returnTotal)
                                                        .occurredAt(ret.getCreatedAt())
                                                        .referenceCode(ret.getReturnCode())
                                                        .build());
                                } else if (ret.getReturnType() == ReturnType.MISORDER) {
                                        lossAmount = lossAmount.add(returnTotal);
                                        lines.add(DailyReceiptLine.builder()
                                                        .lineType(VoucherType.LOSS)
                                                        .description(ret.getReturnCode() + " (오발주 손실)")
                                                        .amount(returnTotal)
                                                        .occurredAt(ret.getCreatedAt())
                                                        .referenceCode(ret.getReturnCode())
                                                        .build());
                                }
                        }
                }

                // 4. 해당 날짜, 해당 가맹점의 유효한 배송 기록(배송 중, 완료) 조회 및 집계
                List<DeliverStatus> validDeliverStatuses = List.of(DeliverStatus.IN_TRANSIT, DeliverStatus.DELIVERED);
                List<Transit> transits = transitRepository.findAllByFranchiseIdAndStatusInAndCreatedAtBetween(
                                franchiseId, validDeliverStatuses, date.atStartOfDay(), date.atTime(LocalTime.MAX));

                // 송장 번호(trackingNumber) 기준으로 그룹화하여 동일 배송은 1건으로 처리
                Map<String, List<Transit>> transitsByTracking = transits.stream()
                                .collect(Collectors.groupingBy(Transit::getTrackingNumber));

                BigDecimal deliveryFee = BigDecimal.ZERO;
                for (Map.Entry<String, List<Transit>> entry : transitsByTracking.entrySet()) {
                        String trackingNumber = entry.getKey();
                        List<Transit> group = entry.getValue();

                        // 1. 배송 모듈 서비스 호출을 위한 OrderInfo 리스트 생성
                        List<OrderInfo> orderInfos = group.stream()
                                        .map(t -> new OrderInfo(null, t.getOrderCode(), t.getWeight(),
                                                        t.getFranchiseId(), null, null))
                                        .toList();

                        // 2. 해당 배송 차량의 배송비 계산 서비스 호출
                        Long vehicleId = group.get(0).getVehicleId();
                        List<DeliveryFeeInfo> feeInfos = transportService.calculateDeliveryFee(orderInfos, vehicleId);

                        // 3. 현재 가맹점의 배송비 반영
                        for (DeliveryFeeInfo feeInfo : feeInfos) {
                                if (feeInfo.franchiseId().equals(franchiseId)) {
                                        BigDecimal fee = feeInfo.deliveryFee();
                                        deliveryFee = deliveryFee.add(fee);

                                        // 상세 영수증 라인에 배송비 추가
                                        lines.add(DailyReceiptLine.builder()
                                                        .lineType(VoucherType.DELIVERY)
                                                        .description("배송비 (송장: " + trackingNumber + ")")
                                                        .amount(fee)
                                                        .occurredAt(group.get(0).getCreatedAt())
                                                        .referenceCode(trackingNumber)
                                                        .build());
                                }
                        }
                }

                log.info("[DEBUG] 배송비 집계 결과 - 건수: {}, 총액: {}", transitsByTracking.size(), deliveryFee);

                log.info("[DEBUG] 최종 집계 결과 - 반품환급액(DEFECT): {}, 손실액(MISORDER): {}", refundAmount, lossAmount);

                // 3.3% 수수료 산출 (정수 자리로 반올림하여 계산의 명확성 확보)
                BigDecimal commissionFee = totalSale.multiply(new BigDecimal("0.033"))
                                .setScale(0, java.math.RoundingMode.HALF_UP);

                log.info("[DEBUG] 수수료 계산 - 매출: {}, 요율: 3.3%, 산출수수료: {}", totalSale, commissionFee);

                // 수수료 내역 추가 (상세 전표 조회용)
                if (commissionFee.compareTo(BigDecimal.ZERO) > 0) {
                        lines.add(DailyReceiptLine.builder()
                                        .lineType(VoucherType.COMMISSION)
                                        .description("판매 수수료 (3.3%)")
                                        .amount(commissionFee)
                                        .occurredAt(date.atTime(23, 59, 59))
                                        .referenceCode("COMM-" + date.toString().replace("-", ""))
                                        .build());
                }

                // Null-safe finalAmount 계산 (모든 항목이 null이 아님을 보장하지만 명시적으로 처리)
                BigDecimal totalDeductions = orderAmount.add(lossAmount).add(commissionFee).add(deliveryFee);
                BigDecimal finalAmount = totalSale.subtract(totalDeductions).add(refundAmount);

                log.info("[DEBUG] 정산 금액 산출 - 매출(+): {}, 환급(+): {}, 공제(-): {}, 최종: {}", 
                                totalSale, refundAmount, totalDeductions, finalAmount);

                // 간소화된 가집계 결과 반환
                return new AggregationResult(
                                DailySettlementReceipt.builder()
                                                .franchiseId(franchiseId)
                                                .settlementDate(date)
                                                .totalSaleAmount(totalSale)
                                                .orderAmount(orderAmount)
                                                .refundAmount(refundAmount) // 상품하자 환급액
                                                .lossAmount(lossAmount) // 오발주 손실액
                                                .deliveryFee(deliveryFee)
                                                .commissionFee(commissionFee)
                                                .adjustmentAmount(BigDecimal.ZERO)
                                                .finalAmount(finalAmount)
                                                .build(),
                                lines);
        }

        // 월별 매출 추이 조회 (그래프용)
        @Transactional(readOnly = true)
        public List<FranchiseDailyGraphResponse> getMonthlySalesTrend(Long franchiseId, YearMonth month) {
                log.info("[DEBUG] getMonthlySalesTrend - franchiseId: {}, month: {}", franchiseId, month);

                LocalDateTime start = month.atDay(1).atStartOfDay();
                LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);

                // 월간 모든 매출 아이템 조회
                List<SalesItem> items = salesItemRepository.findAllBySalesFranchiseIdAndCreatedAtBetween(franchiseId, start, end)
                                .stream()
                                .filter(item -> {
                                        Boolean canceled = item.getSales().getIsCanceled();
                                        return canceled == null || !canceled;
                                })
                                .toList();

                // 일자별 합계 계산
                Map<LocalDate, BigDecimal> dailySums = items.stream()
                                .collect(Collectors.groupingBy(
                                                item -> item.getCreatedAt().toLocalDate(),
                                                Collectors.reducing(BigDecimal.ZERO,
                                                                item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                                                BigDecimal::add)));

                // 해당 월의 모든 날짜에 대해 데이터 생성 (비어있는 날은 0원)
                List<FranchiseDailyGraphResponse> result = new ArrayList<>();
                LocalDate dateLoop = month.atDay(1);
                LocalDate lastDate = month.atEndOfMonth();
                if (lastDate.isAfter(LocalDate.now())) {
                        lastDate = LocalDate.now();
                }

                while (!dateLoop.isAfter(lastDate)) {
                        BigDecimal amount = dailySums.getOrDefault(dateLoop, BigDecimal.ZERO);
                        result.add(new FranchiseDailyGraphResponse(dateLoop, amount));
                        dateLoop = dateLoop.plusDays(1);
                }

                return result;
        }

}
