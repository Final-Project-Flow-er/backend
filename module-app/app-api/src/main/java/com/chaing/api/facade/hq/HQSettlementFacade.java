package com.chaing.api.facade.hq;

import com.chaing.api.dto.franchise.settlement.response.FranchiseSettlementSummaryResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseVoucherResponse;
import com.chaing.api.dto.hq.settlement.request.*;
import com.chaing.api.dto.hq.settlement.response.*;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
import com.chaing.domain.settlements.service.DailySettlementService;
import com.chaing.domain.settlements.service.MonthlySettlementService;
import com.chaing.api.service.settlement.SettlementFileService;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
import com.chaing.domain.businessunits.entity.Franchise;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@Component
@RequiredArgsConstructor
public class HQSettlementFacade {

        private final DailySettlementService dailyService;
        private final MonthlySettlementService monthlyService;
        private final SettlementFileService fileService;
        private final MinioService minioService;
        private final com.chaing.domain.settlements.service.SettlementDocumentService documentService;
        private final com.chaing.domain.settlements.repository.interfaces.SettlementVoucherRepository voucherRepository;
        private final FranchiseRepository franchiseRepository;

        // 1. 일별 조회

        @Transactional(readOnly = true)
        public HQSettlementSummaryResponse getDailySummary(HQSettlementDailySummaryRequest request) {
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDate(request.date(), null);

                BigDecimal totalOrder = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getOrderAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalSale = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getTotalSaleAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCommission = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getCommissionFee)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalDelivery = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getDeliveryFee)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalRefund = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getRefundAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalLoss = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getLossAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 본사 관점 최종 정산 금액 = 발주 매출 + 수수료 수익 + 배송 수익 - 반품 차감액 - 본사 손실
                BigDecimal hqTotalFinal = totalOrder.add(totalCommission).add(totalDelivery)
                                .subtract(totalRefund).subtract(totalLoss);

                return HQSettlementSummaryResponse.of(hqTotalFinal, totalOrder, totalSale, totalCommission,
                                totalDelivery,
                                totalRefund, totalLoss);
        }

        @Transactional(readOnly = true)
        public Page<HQFranchiseSettlementResponse> getDailyFranchises(HQSettlementDailyFranchisesRequest request) {
                // 1. 도메인 서비스에서 날짜 및 키워드로 전체 목록 조회
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDate(request.date(), request.keyword());

                // 2. 가맹점 정보 벌크 조회 (N+1 쿼리 방지)
                List<Long> franchiseIds = receipts.stream()
                                .map(com.chaing.domain.settlements.entity.DailySettlementReceipt::getFranchiseId)
                                .distinct()
                                .collect(Collectors.toList());

                Map<Long, String> franchiseNameMap = franchiseRepository.findAllById(franchiseIds).stream()
                                .collect(Collectors.toMap(Franchise::getFranchiseId, Franchise::getName));

                List<HQFranchiseSettlementResponse> dtos = receipts.stream()
                                .map(r -> {
                                        String franchiseName = franchiseNameMap.getOrDefault(r.getFranchiseId(),
                                                        "Unknown");
                                        return HQFranchiseSettlementResponse.of(
                                                        r.getFranchiseId(),
                                                        franchiseName,
                                                        r.getTotalSaleAmount(),
                                                        r.getOrderAmount(),
                                                        r.getDeliveryFee(),
                                                        r.getCommissionFee(),
                                                        r.getRefundAmount(),
                                                        r.getLossAmount(),
                                                        r.getFinalAmount(),
                                                        com.chaing.domain.settlements.enums.SettlementStatus.CONFIRMED, // 기본값
                                                        r.getSettlementDate());
                                })
                                .collect(Collectors.toList());

                int page = request.page() != null ? request.page() : 0;
                int size = request.size() != null ? request.size() : 20;
                Pageable pageable = PageRequest.of(page, size);

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), dtos.size());

                if (start > dtos.size()) {
                        return new PageImpl<>(List.of(), pageable, dtos.size());
                }

                return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
        }

        @Transactional(readOnly = true)
        public List<HQDailyGraphResponse> getDailyTrend(HQSettlementDailyGraphRequest request) {
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDateRange(request.start(), request.end());

                // 날짜별로 그룹화하여 totalSaleAmount 합산
                Map<LocalDate, BigDecimal> dailySums = receipts.stream()
                                .collect(Collectors.groupingBy(
                                                com.chaing.domain.settlements.entity.DailySettlementReceipt::getSettlementDate,
                                                Collectors.reducing(BigDecimal.ZERO,
                                                                com.chaing.domain.settlements.entity.DailySettlementReceipt::getTotalSaleAmount,
                                                                BigDecimal::add)));

                return dailySums.entrySet().stream()
                                .map(entry -> HQDailyGraphResponse.of(entry.getKey(), entry.getValue()))
                                .sorted((a, b) -> a.date().compareTo(b.date())) // 날짜 오름차순 정렬
                                .collect(Collectors.toList());
        }

        // 2. 월별 조회

        @Transactional(readOnly = true)
        public HQSettlementSummaryResponse getMonthlySummary(HQSettlementMonthlySummaryRequest request) {
                List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                .getAllByMonth(request.month(), null);

                BigDecimal totalOrder = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getOrderAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalSale = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getTotalSaleAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCommission = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getCommissionFee)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalDelivery = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getDeliveryFee)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalRefund = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getRefundAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalLoss = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getLossAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 본사 관점 최종 정산 금액
                BigDecimal hqTotalFinal = totalOrder.add(totalCommission).add(totalDelivery)
                                .subtract(totalRefund).subtract(totalLoss);

                return HQSettlementSummaryResponse.of(hqTotalFinal, totalOrder, totalSale, totalCommission,
                                totalDelivery,
                                totalRefund, totalLoss);
        }

        @Transactional(readOnly = true)
        public Page<HQFranchiseSettlementResponse> getMonthlyFranchises(HQSettlementMonthlyFranchisesRequest request) {
                List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                .getAllByMonth(request.month(), request.keyword());

                // 상태 필터링 처리
                if (request.status() != null) {
                        settlements = settlements.stream()
                                        .filter(s -> s.getStatus() == request.status())
                                        .collect(Collectors.toList());
                }

                // 2. 가맹점 정보 벌크 조회 (N+1 쿼리 방지)
                List<Long> franchiseIds = settlements.stream()
                                .map(com.chaing.domain.settlements.entity.MonthlySettlement::getFranchiseId)
                                .distinct()
                                .collect(Collectors.toList());

                Map<Long, String> franchiseNameMap = franchiseRepository.findAllById(franchiseIds).stream()
                                .collect(Collectors.toMap(Franchise::getFranchiseId, Franchise::getName));

                List<HQFranchiseSettlementResponse> dtos = settlements.stream()
                                .map(s -> {
                                        String franchiseName = franchiseNameMap.getOrDefault(s.getFranchiseId(),
                                                        "Unknown");
                                        return HQFranchiseSettlementResponse.of(
                                                        s.getFranchiseId(),
                                                        franchiseName,
                                                        s.getTotalSaleAmount(),
                                                        s.getOrderAmount(),
                                                        s.getDeliveryFee(),
                                                        s.getCommissionFee(),
                                                        s.getRefundAmount(),
                                                        s.getLossAmount(),
                                                        s.getFinalSettlementAmount(),
                                                        s.getStatus(),
                                                        s.getSettlementMonth().atDay(1)); // 기준일
                                })
                                .collect(Collectors.toList());

                int page = request.page() != null ? request.page() : 0;
                int size = request.size() != null ? request.size() : 20;
                Pageable pageable = PageRequest.of(page, size);

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), dtos.size());

                if (start > dtos.size()) {
                        return new PageImpl<>(List.of(), pageable, dtos.size());
                }

                return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
        }

        @Transactional(readOnly = true)
        public List<HQMonthlyGraphResponse> getMonthlyTrend(HQSettlementMonthlyGraphRequest request) {
                // start ~ end 기간 사이의 모든 월별 정산 데이터 조회

                YearMonth startMonth = YearMonth.from(request.start());
                YearMonth endMonth = YearMonth.from(request.end());

                List<HQMonthlyGraphResponse> result = new java.util.ArrayList<>();
                YearMonth current = startMonth;

                while (!current.isAfter(endMonth)) {
                        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                        .getAllByMonth(current, null);
                        BigDecimal totalSaleAmount = settlements.stream()
                                        .map(com.chaing.domain.settlements.entity.MonthlySettlement::getTotalSaleAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        result.add(HQMonthlyGraphResponse.of(current, totalSaleAmount));
                        current = current.plusMonths(1);
                }

                return result;
        }

        // 3. 단건 가맹점 상세 내역 (Drill-down), 전표 조회

        @Transactional(readOnly = true)
        public FranchiseSettlementSummaryResponse getDailyFranchiseSummary(Long franchiseId,
                        HQSettlementFranchiseDailyDetailRequest request) {
                com.chaing.domain.settlements.entity.DailySettlementReceipt receipt = dailyService
                                .getByFranchiseAndDate(franchiseId, request.date());

                return new FranchiseSettlementSummaryResponse(
                                receipt.getFinalAmount(),
                                receipt.getTotalSaleAmount(),
                                receipt.getRefundAmount(),
                                receipt.getOrderAmount(),
                                receipt.getDeliveryFee(),
                                receipt.getLossAmount(),
                                receipt.getCommissionFee(),
                                receipt.getAdjustmentAmount());
        }

        @Transactional(readOnly = true)
        public FranchiseSettlementSummaryResponse getMonthlyFranchiseSummary(Long franchiseId,
                        HQSettlementFranchiseMonthlyDetailRequest request) {
                com.chaing.domain.settlements.entity.MonthlySettlement settlement = monthlyService
                                .getByFranchiseAndMonth(franchiseId, request.month());

                return new FranchiseSettlementSummaryResponse(
                                settlement.getFinalSettlementAmount(),
                                settlement.getTotalSaleAmount(),
                                settlement.getRefundAmount(),
                                settlement.getOrderAmount(),
                                settlement.getDeliveryFee(),
                                settlement.getLossAmount(),
                                settlement.getCommissionFee(),
                                settlement.getAdjustmentAmount());
        }

        @Transactional(readOnly = true)
        public Page<FranchiseVoucherResponse> getFranchiseVouchers(Long franchiseId, PeriodType period, LocalDate date,
                        YearMonth month, VoucherType type, int page, int size) {

                PageRequest pageable = PageRequest.of(page, size);

                if (period == PeriodType.DAILY) {
                        // 일별: DailySettlementService에서 Receipt 조회 후 ReceiptLine을 Response로 변환
                        com.chaing.domain.settlements.entity.DailySettlementReceipt receipt = dailyService
                                        .getByFranchiseAndDate(franchiseId, date);
                        Page<com.chaing.domain.settlements.entity.DailyReceiptLine> lines = dailyService
                                        .getReceiptLines(receipt.getDailyReceiptId(), type, pageable);

                        return lines.map(line -> new FranchiseVoucherResponse(
                                        line.getReferenceCode(),
                                        line.getLineType(),
                                        line.getDescription(),
                                        line.getQuantity(),
                                        line.getAmount(),
                                        line.getOccurredAt()));

                } else {
                        // 월별: MonthlySettlement 조회 후 SettlementVoucher를 Response로 변환
                        com.chaing.domain.settlements.entity.MonthlySettlement settlement = monthlyService
                                        .getByFranchiseAndMonth(franchiseId, month);
                        // Page 처리를 위해 페이징 쿼리 사용 (또는 리스트 변환 후 페이징)
                        List<com.chaing.domain.settlements.entity.SettlementVoucher> vouchers = voucherRepository
                                        .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());

                        List<FranchiseVoucherResponse> dtos = vouchers.stream()
                                        .filter(v -> type == null || v.getVoucherType() == type)
                                        .map(v -> new FranchiseVoucherResponse(
                                                        v.getReferenceCode(),
                                                        v.getVoucherType(),
                                                        v.getDescription(),
                                                        v.getQuantity(),
                                                        v.getAmount(),
                                                        v.getOccurredAt()))
                                        .collect(Collectors.toList());

                        int start = (int) pageable.getOffset();
                        int end = Math.min((start + pageable.getPageSize()), dtos.size());
                        if (start > dtos.size())
                                return Page.empty();

                        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
                }
        }

        @Transactional(readOnly = true)
        public Page<FranchiseVoucherResponse> getAllVouchers(PeriodType period, LocalDate date, YearMonth month,
                        VoucherType type, int page, int size) {
                PageRequest pageable = PageRequest.of(page, size);

                if (period == PeriodType.DAILY) {
                        List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                        .getAllByDate(date, null);
                        List<FranchiseVoucherResponse> allLines = receipts.stream()
                                        .flatMap(r -> dailyService.getAllReceiptLines(r.getDailyReceiptId()).stream())
                                        .filter(line -> type == null || line.getLineType() == type)
                                        .map(line -> new FranchiseVoucherResponse(
                                                        line.getReferenceCode(),
                                                        line.getLineType(),
                                                        line.getDescription(),
                                                        line.getQuantity(),
                                                        line.getAmount(),
                                                        line.getOccurredAt()))
                                        .collect(Collectors.toList());

                        int start = (int) pageable.getOffset();
                        int end = Math.min((start + pageable.getPageSize()), allLines.size());
                        if (start > allLines.size())
                                return Page.empty();
                        return new PageImpl<>(allLines.subList(start, end), pageable, allLines.size());

                } else {
                        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                        .getAllByMonth(month, null);
                        List<Long> settlementIds = settlements.stream()
                                        .map(com.chaing.domain.settlements.entity.MonthlySettlement::getMonthlySettlementId)
                                        .collect(Collectors.toList());

                        if (settlementIds.isEmpty())
                                return Page.empty();

                        List<com.chaing.domain.settlements.entity.SettlementVoucher> vouchers = voucherRepository
                                        .findAllByMonthlySettlementIdIn(settlementIds);

                        List<FranchiseVoucherResponse> dtos = vouchers.stream()
                                        .filter(v -> type == null || v.getVoucherType() == type)
                                        .map(v -> new FranchiseVoucherResponse(
                                                        v.getReferenceCode(),
                                                        v.getVoucherType(),
                                                        v.getDescription(),
                                                        v.getQuantity(),
                                                        v.getAmount(),
                                                        v.getOccurredAt()))
                                        .collect(Collectors.toList());

                        int start = (int) pageable.getOffset();
                        int end = Math.min((start + pageable.getPageSize()), dtos.size());
                        if (start > dtos.size())
                                return Page.empty();
                        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
                }
        }

        // 4. PDF 및 엑셀 다운로드 (URL 반환)
        @Transactional
        public String getDailyAllSummaryPdf(HQSettlementDailyAllPdfRequest request) {
                log.info("[DEBUG] Facade - getDailyAllSummaryPdf requested for date: {}", request.date());
                // 1. 이미 생성된 문서가 있는지 확인
                java.util.Optional<com.chaing.domain.settlements.entity.SettlementDocument> existingDoc = documentService
                                .getHQDailyDocument(request.date());
                if (existingDoc.isPresent()) {
                        return minioService.getFileUrl(existingDoc.get().getObjectKey(), BucketName.SETTLEMENTS);
                }

                try {
                        // 2. 해당 날짜의 모든 가맹점 정산 요약 데이터 조회
                        List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                        .getAllByDate(request.date(), null);

                        if (receipts.isEmpty()) {
                                // 상황 2: 정산 데이터 자체가 없음 (휴무일 등)
                                log.warn("[WARN] No receipts found for HQ Daily PDF: {}", request.date());
                                throw new com.chaing.domain.settlements.exception.SettlementException(
                                                com.chaing.domain.settlements.exception.SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                        }

                        // 3. 파일 생성 서비스 호출
                        byte[] pdfBytes = fileService.createHQSettlementDailyPdf(request.date(), receipts);

                        // 4. MinIO 업로드
                        String fileName = "settlement/daily/HQ_Daily_Settlement_" + request.date() + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);
                        log.info("=================> fileUrl: {}", fileUrl);
                        // 5. 메타데이터 저장
                        documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                        .periodType(PeriodType.DAILY)
                                        .documentType(com.chaing.domain.settlements.enums.DocumentType.HQ_DAILY_SUM)
                                        .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.HQ)
                                        .settlementDate(request.date())
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/pdf")
                                        .fileSize((long) pdfBytes.length)
                                        .build());

                        return fileUrl;
                } catch (com.chaing.domain.settlements.exception.SettlementException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate HQ Daily PDF: ", e);
                        throw new com.chaing.domain.settlements.exception.SettlementException(
                                        com.chaing.domain.settlements.exception.SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getMonthlyAllSummaryPdf(HQSettlementMonthlyAllPdfRequest request) {
                // 1. 이미 생성된 문서가 있는지 확인
                java.util.Optional<com.chaing.domain.settlements.entity.SettlementDocument> existingDoc = documentService
                                .getHQMonthlyDocument(request.month());
                if (existingDoc.isPresent()) {
                        return minioService.getFileUrl(existingDoc.get().getObjectKey(), BucketName.SETTLEMENTS);
                }

                try {
                        // 2. 해당 월의 모든 가맹점 정산 데이터 조회
                        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                        .getAllByMonth(request.month(), null);

                        if (settlements.isEmpty()) {
                                throw new com.chaing.domain.settlements.exception.SettlementException(
                                                com.chaing.domain.settlements.exception.SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                        }

                        // 3. 파일 생성 서비스 호출
                        byte[] pdfBytes = fileService.createHQSettlementMonthlyPdf(request.month(), settlements);

                        // 4. MinIO 업로드
                        String fileName = "settlement/monthly/HQ_Monthly_Report_" + request.month() + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        // 5. 메타데이터 저장
                        documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(com.chaing.domain.settlements.enums.DocumentType.HQ_MONTHLY_SUM)
                                        .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.HQ)
                                        .settlementMonth(request.month())
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/pdf")
                                        .fileSize((long) pdfBytes.length)
                                        .build());

                        return fileUrl;
                } catch (com.chaing.domain.settlements.exception.SettlementException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate HQ Monthly PDF: ", e);
                        throw new com.chaing.domain.settlements.exception.SettlementException(
                                        com.chaing.domain.settlements.exception.SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getDailyFranchiseReceiptPdf(Long franchiseId,
                        HQSettlementFranchiseDailyReceiptPdfRequest request) {
                try {
                        com.chaing.domain.settlements.entity.DailySettlementReceipt receipt = dailyService
                                        .getByFranchiseAndDate(franchiseId, request.date());

                        // 1. 이미 생성된 문서가 있는지 확인
                        java.util.Optional<com.chaing.domain.settlements.entity.SettlementDocument> existingDoc = documentService
                                        .getDailyDocument(receipt.getDailyReceiptId());
                        if (existingDoc.isPresent()) {
                                return minioService.getFileUrl(existingDoc.get().getObjectKey(),
                                                BucketName.SETTLEMENTS);
                        }

                        List<com.chaing.domain.settlements.entity.DailyReceiptLine> lines = dailyService
                                        .getAllReceiptLines(receipt.getDailyReceiptId());

                        byte[] pdfBytes = fileService.createDailyReceiptPdf(receipt, lines);

                        String fileName = "settlement/daily/Franchise_" + franchiseId + "_Receipt_" + request.date()
                                        + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        // 2. 메타데이터 저장
                        documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                        .dailyReceiptId(receipt.getDailyReceiptId())
                                        .periodType(PeriodType.DAILY)
                                        .documentType(com.chaing.domain.settlements.enums.DocumentType.RECEIPT_PDF)
                                        .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.FRANCHISE)
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
                } catch (com.chaing.domain.settlements.exception.SettlementException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate Daily Franchise Receipt PDF: ", e);
                        throw new com.chaing.domain.settlements.exception.SettlementException(
                                        com.chaing.domain.settlements.exception.SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getMonthlyFranchiseReceiptPdf(Long franchiseId,
                        HQSettlementFranchiseMonthlyReceiptPdfRequest request) {
                try {
                        com.chaing.domain.settlements.entity.MonthlySettlement settlement = monthlyService
                                        .getByFranchiseAndMonth(franchiseId, request.month());

                        // 1. 이미 생성된 문서가 있는지 확인
                        List<com.chaing.domain.settlements.entity.SettlementDocument> documents = documentService
                                        .getMonthlyDocuments(settlement.getMonthlySettlementId());
                        String existingUrl = documents.stream()
                                        .filter(doc -> doc
                                                        .getDocumentType() == com.chaing.domain.settlements.enums.DocumentType.RECEIPT_PDF)
                                        .findFirst()
                                        .map(com.chaing.domain.settlements.entity.SettlementDocument::getFileUrl)
                                        .orElse(null);

                        if (existingUrl != null) {
                                return minioService.getFileUrl(documents.stream()
                                                .filter(doc -> doc
                                                                .getDocumentType() == com.chaing.domain.settlements.enums.DocumentType.RECEIPT_PDF)
                                                .findFirst()
                                                .get().getObjectKey(), BucketName.SETTLEMENTS);
                        }

                        List<com.chaing.domain.settlements.entity.SettlementVoucher> vouchers = voucherRepository
                                        .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());

                        byte[] pdfBytes = fileService.createMonthlyReceiptPdf(settlement, vouchers);

                        String fileName = "settlement/monthly/Franchise_" + franchiseId + "_Receipt_" + request.month()
                                        + "_"
                                        + System.currentTimeMillis() + ".pdf";
                        minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                        // 2. 메타데이터 저장
                        documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                        .monthlySettlementId(settlement.getMonthlySettlementId())
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(com.chaing.domain.settlements.enums.DocumentType.RECEIPT_PDF)
                                        .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.FRANCHISE)
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
                } catch (com.chaing.domain.settlements.exception.SettlementException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate Monthly Franchise Receipt PDF: ", e);
                        throw new com.chaing.domain.settlements.exception.SettlementException(
                                        com.chaing.domain.settlements.exception.SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }

        @Transactional
        public String getMonthlyExcel(HQSettlementMonthlyExcelRequest request) {
                try {
                        // 1. 해당 월의 모든 가맹점 정산 데이터 조회
                        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                        .getAllByMonth(request.month(), null);

                        if (settlements.isEmpty()) {
                                throw new com.chaing.domain.settlements.exception.SettlementException(
                                                com.chaing.domain.settlements.exception.SettlementErrorCode.SETTLEMENT_DATA_EMPTY);
                        }

                        // 2. 파일 생성 서비스 호출 (Excel 생성)
                        byte[] excelBytes = fileService.createMonthlySettlementExcel(settlements);

                        // 3. MinIO 업로드
                        String fileName = "settlement/monthly/HQ_Monthly_Settlement_" + request.month() + "_"
                                        + System.currentTimeMillis() + ".xlsx";
                        minioService.uploadFile(excelBytes, fileName,
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        BucketName.SETTLEMENTS);

                        // 4. 메타데이터 저장
                        String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);
                        documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                        .periodType(PeriodType.MONTHLY)
                                        .documentType(com.chaing.domain.settlements.enums.DocumentType.VOUCHER_EXCEL)
                                        .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.HQ)
                                        .settlementMonth(request.month())
                                        .storageProvider("MINIO")
                                        .bucket(BucketName.SETTLEMENTS.getBucketName())
                                        .objectKey(fileName)
                                        .fileUrl(fileUrl)
                                        .fileName(fileName.substring(fileName.lastIndexOf("/") + 1))
                                        .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                        .fileSize((long) excelBytes.length)
                                        .build());

                        return fileUrl;
                } catch (com.chaing.domain.settlements.exception.SettlementException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to generate Monthly Excel: ", e);
                        throw new com.chaing.domain.settlements.exception.SettlementException(
                                        com.chaing.domain.settlements.exception.SettlementErrorCode.DOCUMENT_GENERATION_FAILED);
                }
        }
}
