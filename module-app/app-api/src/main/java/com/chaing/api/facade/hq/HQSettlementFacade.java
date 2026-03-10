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
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementFacade {

        private final DailySettlementService dailyService;
        private final MonthlySettlementService monthlyService;
        private final SettlementFileService fileService;

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
                return "https://dummy-url.com/daily-all-summary.pdf";
        }

        public String getMonthlyAllSummaryPdf(HQSettlementMonthlyAllPdfRequest request) {
                return "https://dummy-url.com/monthly-all-summary.pdf";
        }

        public String getDailyFranchiseReceiptPdf(Long franchiseId,
                        HQSettlementFranchiseDailyReceiptPdfRequest request) {
                return "https://dummy-url.com/daily-franchise-receipt.pdf";
        }

        public String getMonthlyFranchiseReceiptPdf(Long franchiseId,
                        HQSettlementFranchiseMonthlyReceiptPdfRequest request) {
                return "https://dummy-url.com/monthly-franchise-receipt.pdf";
        }

        public String getMonthlyExcel(HQSettlementMonthlyExcelRequest request) {
                return "https://dummy-url.com/monthly-excel.xlsx";
        }
}
