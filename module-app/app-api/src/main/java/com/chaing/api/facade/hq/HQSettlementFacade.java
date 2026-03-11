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
@Transactional(readOnly = true)
public class HQSettlementFacade {

        private final DailySettlementService dailyService;
        private final MonthlySettlementService monthlyService;
        private final SettlementFileService fileService;
        private final MinioService minioService;
        private final com.chaing.domain.settlements.service.SettlementDocumentService documentService;
        private final com.chaing.domain.settlements.repository.interfaces.SettlementVoucherRepository voucherRepository;

        // 1. 일별 조회

        public HQSettlementSummaryResponse getDailySummary(HQSettlementDailySummaryRequest request) {
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDate(request.date(), null);

                long finalAmount = receipts.stream().mapToLong(r -> r.getFinalAmount().longValue()).sum();
                long orderAmount = receipts.stream().mapToLong(r -> r.getOrderAmount().longValue()).sum();
                long saleAmount = receipts.stream().mapToLong(r -> r.getTotalSaleAmount().longValue()).sum();
                long commissionFee = receipts.stream().mapToLong(r -> r.getCommissionFee().longValue()).sum();
                long deliveryFee = receipts.stream().mapToLong(r -> r.getDeliveryFee().longValue()).sum();
                long refundAmount = receipts.stream().mapToLong(r -> r.getRefundAmount().longValue()).sum();
                long lossAmount = receipts.stream().mapToLong(r -> r.getLossAmount().longValue()).sum();

                return HQSettlementSummaryResponse.of(
                                finalAmount, orderAmount, saleAmount, commissionFee, deliveryFee, refundAmount,
                                lossAmount);
        }

        public Page<HQFranchiseSettlementResponse> getDailyFranchises(HQSettlementDailyFranchisesRequest request) {
                // 1. 도메인 서비스에서 날짜 및 키워드로 전체 목록 조회
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDate(request.date(), request.keyword());

                List<HQFranchiseSettlementResponse> dtos = receipts.stream()
                                .map(r -> HQFranchiseSettlementResponse.of(
                                                r.getFranchiseId(),
                                                "가맹점명 (추후 연동)", // TODO: Franchise API 연동하여 실제 이름 세팅
                                                r.getTotalSaleAmount().longValue(),
                                                r.getFinalAmount().longValue(),
                                                null, // DailyReceipt에는 상태(status) 필드가 없음
                                                r.getSettlementDate()))
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

        public List<HQDailyGraphResponse> getDailyTrend(HQSettlementDailyGraphRequest request) {
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDateRange(request.start(), request.end());

                // 날짜별로 그룹화하여 totalSaleAmount 합산
                Map<LocalDate, Long> dailySums = receipts.stream()
                                .collect(Collectors.groupingBy(
                                                com.chaing.domain.settlements.entity.DailySettlementReceipt::getSettlementDate,
                                                Collectors.summingLong(r -> r.getTotalSaleAmount().longValue())));

                return dailySums.entrySet().stream()
                                .map(entry -> HQDailyGraphResponse.of(entry.getKey(), entry.getValue()))
                                .sorted((a, b) -> a.date().compareTo(b.date())) // 날짜 오름차순 정렬
                                .collect(Collectors.toList());
        }

        // 2. 월별 조회

        public HQSettlementSummaryResponse getMonthlySummary(HQSettlementMonthlySummaryRequest request) {
                List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                .getAllByMonth(request.month(), null);

                long finalAmount = settlements.stream().mapToLong(s -> s.getFinalSettlementAmount().longValue()).sum();
                long orderAmount = settlements.stream().mapToLong(s -> s.getOrderAmount().longValue()).sum();
                long saleAmount = settlements.stream().mapToLong(s -> s.getTotalSaleAmount().longValue()).sum();
                long commissionFee = settlements.stream().mapToLong(s -> s.getCommissionFee().longValue()).sum();
                long deliveryFee = settlements.stream().mapToLong(s -> s.getDeliveryFee().longValue()).sum();
                long refundAmount = settlements.stream().mapToLong(s -> s.getRefundAmount().longValue()).sum();
                long lossAmount = settlements.stream().mapToLong(s -> s.getLossAmount().longValue()).sum();

                return HQSettlementSummaryResponse.of(
                                finalAmount, orderAmount, saleAmount, commissionFee, deliveryFee, refundAmount,
                                lossAmount);
        }

        public Page<HQFranchiseSettlementResponse> getMonthlyFranchises(HQSettlementMonthlyFranchisesRequest request) {
                List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                .getAllByMonth(request.month(), request.keyword());

                // 상태 필터링 처리
                if (request.status() != null) {
                        settlements = settlements.stream()
                                        .filter(s -> s.getStatus() == request.status())
                                        .collect(Collectors.toList());
                }

                List<HQFranchiseSettlementResponse> dtos = settlements.stream()
                                .map(s -> HQFranchiseSettlementResponse.of(
                                                s.getFranchiseId(),
                                                "가맹점명 (추후 연동)", // TODO: Franchise API 연동
                                                s.getTotalSaleAmount().longValue(),
                                                s.getFinalSettlementAmount().longValue(),
                                                s.getStatus(),
                                                s.getSettlementMonth().atEndOfMonth()))
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

        public List<HQMonthlyGraphResponse> getMonthlyTrend(HQSettlementMonthlyGraphRequest request) {
                // start ~ end 기간 사이의 모든 월별 정산 데이터 조회

                YearMonth startMonth = YearMonth.from(request.start());
                YearMonth endMonth = YearMonth.from(request.end());

                List<HQMonthlyGraphResponse> result = new java.util.ArrayList<>();
                YearMonth current = startMonth;

                while (!current.isAfter(endMonth)) {
                        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                        .getAllByMonth(current, null);
                        long totalSaleAmount = settlements.stream()
                                        .mapToLong(s -> s.getTotalSaleAmount().longValue())
                                        .sum();

                        result.add(HQMonthlyGraphResponse.of(current, totalSaleAmount));
                        current = current.plusMonths(1);
                }

                return result;
        }

        // 3. 단건 가맹점 상세 내역 (Drill-down), 전표 조회

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
                                receipt.getCommissionFee());
        }

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
                                settlement.getCommissionFee());
        }

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

                        return Page.empty();
                }
        }

        // 4. PDF 및 엑셀 다운로드 (URL 반환)

        public String getDailyAllSummaryPdf(HQSettlementDailyAllPdfRequest request) {
                log.info("[DEBUG] Facade - getDailyAllSummaryPdf requested for date: {}", request.date());
                // 1. 이미 생성된 문서가 있는지 확인
                com.chaing.domain.settlements.entity.SettlementDocument existingDoc = documentService
                                .getHQDailyDocument(request.date());
                if (existingDoc != null) {
                        return existingDoc.getFileUrl();
                }

                // 2. 해당 날짜의 모든 가맹점 정산 요약 데이터 조회
                List<com.chaing.domain.settlements.entity.DailySettlementReceipt> receipts = dailyService
                                .getAllByDate(request.date(), null);

                if (receipts.isEmpty()) {
                        return "데이터가 없어 PDF를 생성할 수 없습니다.";
                }

                // 3. 파일 생성 서비스 호출 (모든 가공점 데이터 aggregation은 서비스 내부에서 처리하도록 설계 변경됨)
                byte[] pdfBytes = fileService.createHQSettlementDailyPdf(request.date(), receipts);

                // 4. MinIO 업로드
                String fileName = "settlement/daily/HQ_Daily_Settlement_" + request.date() + "_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 5. 메타데이터 저장
                documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                .periodType(PeriodType.DAILY)
                                .documentType(com.chaing.domain.settlements.enums.DocumentType.HQ_DAILY_SUMMARY_PDF)
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
        }

        public String getMonthlyAllSummaryPdf(HQSettlementMonthlyAllPdfRequest request) {
                // 1. 이미 생성된 문서가 있는지 확인
                com.chaing.domain.settlements.entity.SettlementDocument existingDoc = documentService
                                .getHQMonthlyDocument(request.month());
                if (existingDoc != null) {
                        return existingDoc.getFileUrl();
                }

                // 2. 해당 월의 모든 가맹점 정산 데이터 조회
                List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                .getAllByMonth(request.month(), null);

                if (settlements.isEmpty()) {
                        return "데이터가 없어 PDF를 생성할 수 없습니다.";
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
                                .documentType(com.chaing.domain.settlements.enums.DocumentType.HQ_MONTHLY_SUMMARY_PDF)
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
        }

        public String getDailyFranchiseReceiptPdf(Long franchiseId,
                        HQSettlementFranchiseDailyReceiptPdfRequest request) {
                log.info("[DEBUG] Facade - getDailyFranchiseReceiptPdf requested for franchiseId: {}, date: {}",
                                franchiseId, request.date());
                com.chaing.domain.settlements.entity.DailySettlementReceipt receipt = dailyService
                                .getByFranchiseAndDate(franchiseId, request.date());

                // 1. 이미 생성된 문서가 있는지 확인
                com.chaing.domain.settlements.entity.SettlementDocument existingDoc = documentService
                                .getDailyDocument(receipt.getDailyReceiptId());
                if (existingDoc != null) {
                        return existingDoc.getFileUrl();
                }

                List<com.chaing.domain.settlements.entity.DailyReceiptLine> lines = dailyService
                                .getAllReceiptLines(receipt.getDailyReceiptId());

                byte[] pdfBytes = fileService.createDailyReceiptPdf(receipt, lines);

                String fileName = "settlement/daily/Franchise_" + franchiseId + "_Receipt_" + request.date() + "_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 2. 메타데이터 저장
                documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                .dailyReceiptId(receipt.getDailyReceiptId())
                                .periodType(PeriodType.DAILY)
                                .documentType(com.chaing.domain.settlements.enums.DocumentType.RECEIPT_PDF)
                                .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.FRANCHISE)
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

        public String getMonthlyFranchiseReceiptPdf(Long franchiseId,
                        HQSettlementFranchiseMonthlyReceiptPdfRequest request) {
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
                        return existingUrl;
                }

                List<com.chaing.domain.settlements.entity.SettlementVoucher> vouchers = voucherRepository
                                .findAllByMonthlySettlementId(settlement.getMonthlySettlementId());

                byte[] pdfBytes = fileService.createMonthlyReceiptPdf(settlement, vouchers);

                String fileName = "settlement/monthly/Franchise_" + franchiseId + "_Receipt_" + request.month() + "_"
                                + System.currentTimeMillis() + ".pdf";
                minioService.uploadFile(pdfBytes, fileName, "application/pdf", BucketName.SETTLEMENTS);
                String fileUrl = minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);

                // 2. 메타데이터 저장
                documentService.save(com.chaing.domain.settlements.entity.SettlementDocument.builder()
                                .monthlySettlementId(settlement.getMonthlySettlementId())
                                .periodType(PeriodType.MONTHLY)
                                .documentType(com.chaing.domain.settlements.enums.DocumentType.RECEIPT_PDF)
                                .documentOwner(com.chaing.domain.settlements.enums.DocumentOwner.FRANCHISE)
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

        public String getMonthlyExcel(HQSettlementMonthlyExcelRequest request) {
                // 1. 해당 월의 모든 가맹점 정산 데이터 조회
                List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                                .getAllByMonth(request.month(), null);

                if (settlements.isEmpty()) {
                        return "데이터가 없어 엑셀을 생성할 수 없습니다.";
                }

                // 2. 파일 생성 서비스 호출 (Excel 생성)
                byte[] excelBytes = fileService.createMonthlySettlementExcel(settlements);

                // 3. MinIO 업로드
                String fileName = "settlement/monthly/HQ_Monthly_Settlement_" + request.month() + "_"
                                + System.currentTimeMillis() + ".xlsx";
                minioService.uploadFile(excelBytes, fileName,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                BucketName.SETTLEMENTS);

                // 4. 실제 연동된 URL 반환
                return minioService.getFileUrl(fileName, BucketName.SETTLEMENTS);
        }
}
