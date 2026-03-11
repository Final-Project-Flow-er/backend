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
import com.chaing.domain.sales.entity.Sales;
import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.repository.FranchiseSalesItemRepository;
import com.chaing.domain.sales.repository.FranchiseSalesRepository;
import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.enums.DocumentOwner;
import com.chaing.domain.settlements.enums.DocumentType;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
import com.chaing.domain.settlements.service.DailySettlementService;
import com.chaing.domain.settlements.service.MonthlySettlementService;
import com.chaing.domain.settlements.service.SettlementDocumentService;
import com.chaing.domain.settlements.repository.interfaces.SettlementVoucherRepository;
import com.chaing.domain.settlements.entity.SettlementVoucher;
import com.chaing.api.service.settlement.SettlementFileService;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.MinioService;
import com.chaing.domain.settlements.entity.SettlementDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseSettlementFacade {

        private final DailySettlementService dailyService;
        private final MonthlySettlementService monthlyService;
        private final SettlementDocumentService documentService;
        private final MinioService minioService;
        private final SettlementFileService fileService;
        private final SettlementVoucherRepository voucherRepository;

        // 다른 도메인 Repository (메서드 추가 요청 후 사용)
        private final FranchiseSalesRepository salesRepository;
        private final FranchiseSalesItemRepository salesItemRepository;
        private final FranchiseOrderRepository orderRepository;
        private final FranchiseOrderItemRepository orderItemRepository;

        // 일별 정산 요약
        public FranchiseSettlementSummaryResponse getDailySummary(Long franchiseId, LocalDate date) {
                DailySettlementReceipt r = dailyService.getByFranchiseAndDate(franchiseId, date);
                return toSummary(r);
        }

        // 일별 매출 top5, 전체
        public List<FranchiseSalesItemResponse> getDailySalesItems(
                        Long franchiseId, LocalDate date, Integer limit) {
                // Sales에서 해당 날짜 판매 목록 가져오기
                List<Sales> salesList = salesRepository
                                .findAllByFranchiseIdAndIsCanceledFalseAndCreatedAtBetween(
                                                franchiseId,
                                                date.atStartOfDay(),
                                                date.atTime(23, 59, 59));
                // SalesItem 가져오기
                List<Long> salesIds = salesList.stream()
                                .map(Sales::getSalesId).toList();
                List<SalesItem> items = salesItemRepository.findAllBySalesSalesIdIn(salesIds);

                // 상품별 집계 (상품명 기준 그룹핑)
                return aggregateSalesItems(items, limit);
        }

        // 일별 발주 top5, 전체
        public List<FranchiseOrderItemResponse> getDailyOrderItems(
                        Long franchiseId, LocalDate date, Integer limit) {
                // Orders에서 해당 날짜 발주 목록 (ACCEPTED만)
                List<FranchiseOrder> orders = orderRepository
                                .findAllByFranchiseIdAndOrderStatusAndCreatedAtBetween(
                                                franchiseId,
                                                FranchiseOrderStatus.ACCEPTED,
                                                date.atStartOfDay(),
                                                date.atTime(23, 59, 59));
                // OrderItem 가져오기
                List<Long> orderIds = orders.stream()
                                .map(FranchiseOrder::getFranchiseOrderId).toList();
                List<FranchiseOrderItem> items = orderItemRepository
                                .findAllByFranchiseOrderFranchiseOrderIdIn(orderIds);
                // 상품별 집계
                return aggregateOrderItems(items, limit);
        }

        // 월별 정산 요약
        public FranchiseSettlementSummaryResponse getMonthlySummary(Long franchiseId, YearMonth month) {
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
        }

        // 월별 매출 현황 top5, 전체
        public List<FranchiseSalesItemResponse> getMonthlySalesItems(
                        Long franchiseId, YearMonth month, Integer limit) {
                LocalDate start = month.atDay(1);
                LocalDate end = month.atEndOfMonth();
                List<Sales> salesList = salesRepository
                                .findAllByFranchiseIdAndIsCanceledFalseAndCreatedAtBetween(
                                                franchiseId,
                                                start.atStartOfDay(),
                                                end.atTime(23, 59, 59));
                List<Long> salesIds = salesList.stream()
                                .map(Sales::getSalesId).toList();
                List<SalesItem> items = salesItemRepository.findAllBySalesSalesIdIn(salesIds);
                return aggregateSalesItems(items, limit);
        }

        // 월별 발주 top5, 전체
        public List<FranchiseOrderItemResponse> getMonthlyOrderItems(
                        Long franchiseId, YearMonth month, Integer limit) {
                LocalDate start = month.atDay(1);
                LocalDate end = month.atEndOfMonth();
                List<FranchiseOrder> orders = orderRepository
                                .findAllByFranchiseIdAndOrderStatusAndCreatedAtBetween(
                                                franchiseId,
                                                FranchiseOrderStatus.ACCEPTED,
                                                start.atStartOfDay(),
                                                end.atTime(23, 59, 59));
                List<Long> orderIds = orders.stream()
                                .map(FranchiseOrder::getFranchiseOrderId).toList();
                List<FranchiseOrderItem> items = orderItemRepository
                                .findAllByFranchiseOrderFranchiseOrderIdIn(orderIds);
                return aggregateOrderItems(items, limit);
        }

        // 월별 일자 매출 추이 그래프
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
        public Page<FranchiseVoucherResponse> getVouchers(
                        Long franchiseId, PeriodType period,
                        LocalDate date, YearMonth month,
                        VoucherType type, Pageable pageable) {
                if (period == PeriodType.DAILY) {
                        DailySettlementReceipt receipt = dailyService.getByFranchiseAndDate(franchiseId, date);
                        Page<DailyReceiptLine> lines = dailyService.getReceiptLines(receipt.getDailyReceiptId(), type,
                                        pageable);
                        return lines.map(this::toVoucherResponse);
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
                // 1. 해당 가맹점의 일별 정산 데이터 조회
                DailySettlementReceipt receipt = dailyService.getByFranchiseAndDate(franchiseId, date);

                // 2. 해당 정산 ID로 이미 생성된 문서가 있는지 확인
                SettlementDocument existingDoc = documentService.getDailyDocument(receipt.getDailyReceiptId());
                if (existingDoc != null) {
                        return existingDoc.getFileUrl();
                }

                // 3. 문서가 없으면 실시간 생성
                List<DailyReceiptLine> lines = dailyService.getAllReceiptLines(receipt.getDailyReceiptId());
                byte[] pdfBytes = fileService.createDailyReceiptPdf(receipt, lines);

                // 4. MinIO 업로드
                String fileName = "settlement/daily/FR_" + franchiseId + "_Daily_" + date + "_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 5. DB에 메타데이터 저장
                SettlementDocument newDoc = SettlementDocument.builder()
                                .periodType(PeriodType.DAILY)
                                .documentType(DocumentType.RECEIPT_PDF)
                                .documentOwner(DocumentOwner.FRANCHISE)
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
        }

        public String getMonthlyReceiptPdf(Long franchiseId, YearMonth month) {
                // 1. 해당 가맹점의 월별 정산 데이터 조회
                MonthlySettlement settlement = monthlyService.getByFranchiseAndMonth(franchiseId, month);

                // 2. 해당 월별 정산에 연관된 모든 문서 목록 조회
                List<com.chaing.domain.settlements.entity.SettlementDocument> documents = documentService
                                .getMonthlyDocuments(settlement.getMonthlySettlementId());

                // 3. RECEIPT_PDF 문서가 이미 있는지 확인
                String existingUrl = documents.stream()
                                .filter(doc -> doc.getDocumentType() == DocumentType.RECEIPT_PDF)
                                .findFirst()
                                .map(com.chaing.domain.settlements.entity.SettlementDocument::getFileUrl)
                                .orElse(null);

                if (existingUrl != null) {
                        return existingUrl;
                }

                // 4. 없으면 실시간 생성
                List<com.chaing.domain.settlements.entity.SettlementVoucher> vouchers = voucherRepository
                                .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());
                byte[] pdfBytes = fileService.createMonthlyReceiptPdf(settlement, vouchers);

                // 5. MinIO 업로드
                String fileName = "settlement/monthly/Franchise_" + franchiseId + "_Monthly_Receipt_" + month + "_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);

                // 6. DB 메타데이터 저장
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);
                documentService.save(SettlementDocument.builder()
                                .monthlySettlementId(settlement.getMonthlySettlementId())
                                .periodType(PeriodType.MONTHLY)
                                .documentType(DocumentType.RECEIPT_PDF)
                                .documentOwner(DocumentOwner.FRANCHISE)
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

        @Transactional
        public String getMonthlyVouchersExcel(Long franchiseId, YearMonth month) {
                // 1. 해당 가맹점의 월별 정산 데이터 조회
                MonthlySettlement settlement = monthlyService.getByFranchiseAndMonth(franchiseId, month);

                // 2. 이미 생성된 엑셀 문서가 있는지 확인
                List<SettlementDocument> documents = documentService
                                .getMonthlyDocuments(settlement.getMonthlySettlementId());
                if (documents != null) {
                        var existingExcel = documents.stream()
                                        .filter(doc -> doc.getDocumentType() == DocumentType.VOUCHER_EXCEL)
                                        .findFirst();
                        if (existingExcel.isPresent()) {
                                return existingExcel.get().getFileUrl();
                        }
                }

                // 3. 없으면 실시간 생성 (전표 데이터 조회)
                // TODO: 페이징 없이 전체 조회용 메서드 필요. 일단 mock이나 기존 repository 활용
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
                                .monthlySettlementId(settlement.getMonthlySettlementId())
                                .storageProvider("MINIO")
                                .bucket(BucketName.SETTLEMENTS.getBucketName())
                                .objectKey(fileName)
                                .fileUrl(fileUrl)
                                .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                .contentType("application/vnd.ms-excel")
                                .fileSize((long) excelBytes.length)
                                .build();
                documentService.save(newDoc);

                return fileUrl;
        }

}
