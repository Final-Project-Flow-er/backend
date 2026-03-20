package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.entity.SettlementVoucher;
import com.chaing.domain.settlements.service.SettlementFileService;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.colors.ColorConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class SettlementFileServiceImpl implements SettlementFileService {

    @Override
    public byte[] createDailyReceiptPdf(DailySettlementReceipt receipt, List<DailyReceiptLine> lines, String franchiseName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            PdfFont font = loadKoreanFont();
            document.setFont(font);
            document.setFontSize(9);

            // 1. 헤더 영역
            document.add(new Paragraph("일일 정산 영수증 (공급증)")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));
            
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            metaTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("가맹점: " + franchiseName + " (ID: " + receipt.getFranchiseId() + ")")).setBorder(Border.NO_BORDER));
            metaTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("No. " + receipt.getDailyReceiptId() + "\n정산일자: " + receipt.getSettlementDate())).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            document.add(metaTable.setMarginBottom(15));

            // 2. 공급자 정보
            Table providerTable = new Table(UnitValue.createPercentArray(new float[]{15, 35, 15, 35})).useAllAvailableWidth();
            addInfoCell(providerTable, "사업자번호", "123-45-67890", true);
            addInfoCell(providerTable, "상호", "(주)채잉 본사", false);
            addInfoCell(providerTable, "대표자", "김채잉", true);
            addInfoCell(providerTable, "주소", "서울 서초구 반포동 123", false);
            document.add(providerTable.setMarginBottom(20));

            // 3. 정산 요약 영역
            document.add(new Paragraph("[ 정산 요약 ]").setBold().setMarginBottom(5));
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addSummaryRow(summaryTable, "1. 총 매출액", receipt.getTotalSaleAmount());
            addSummaryRow(summaryTable, "2. 반품 환급액 (+)", receipt.getRefundAmount());
            addSummaryRow(summaryTable, "3. 발주 대금 (-)", receipt.getOrderAmount().negate());
            addSummaryRow(summaryTable, "4. 정산 수수료 (-)", receipt.getCommissionFee().negate());
            addSummaryRow(summaryTable, "5. 배송비 (-)", receipt.getDeliveryFee().negate());
            addSummaryRow(summaryTable, "6. 본사 손실액 (-)", receipt.getLossAmount().negate());
            addSummaryRow(summaryTable, "7. 기타 조정", receipt.getAdjustmentAmount());
            
            com.itextpdf.layout.element.Cell totalLabelCell = new com.itextpdf.layout.element.Cell().add(new Paragraph("최종 정산 금액").setBold().setFontSize(11))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(5).setBorder(new SolidBorder(0.5f));
            com.itextpdf.layout.element.Cell totalValueCell = new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(receipt.getFinalAmount())).setBold().setFontSize(11))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(5).setBorder(new SolidBorder(0.5f));
            summaryTable.addCell(totalLabelCell);
            summaryTable.addCell(totalValueCell);
            document.add(summaryTable.setMarginBottom(25));

            // 4. 매출 상세 내역 (VoucherType.SALES)
            document.add(new Paragraph("[ 매출 현황 상세 ]").setBold().setMarginBottom(5));
            float[] detailWidths = {20, 40, 10, 15, 15};
            Table salesTable = new Table(UnitValue.createPercentArray(detailWidths)).useAllAvailableWidth();
            addDetailHeader(salesTable, "시간");
            
            List<DailyReceiptLine> salesLines = lines.stream().filter(l -> l.getLineType() == com.chaing.domain.settlements.enums.VoucherType.SALES).toList();
            if (salesLines.isEmpty()) {
                salesTable.addCell(new com.itextpdf.layout.element.Cell(1, 5).add(new Paragraph("매출 내역이 없습니다.").setTextAlignment(TextAlignment.CENTER)).setPadding(5).setBorder(new SolidBorder(0.5f)));
            } else {
                for (DailyReceiptLine line : salesLines) {
                    addDetailRow(salesTable, line);
                }
            }
            document.add(salesTable.setMarginBottom(20));

            // 5. 발주 내역 상세 (VoucherType.ORDER)
            document.add(new Paragraph("[ 발주 내역 상세 ]").setBold().setMarginBottom(5));
            Table purchaseTable = new Table(UnitValue.createPercentArray(detailWidths)).useAllAvailableWidth();
            addDetailHeader(purchaseTable, "시간");
            
            List<DailyReceiptLine> purchaseLines = lines.stream().filter(l -> l.getLineType() == com.chaing.domain.settlements.enums.VoucherType.ORDER).toList();
            if (purchaseLines.isEmpty()) {
                purchaseTable.addCell(new com.itextpdf.layout.element.Cell(1, 5).add(new Paragraph("발주 내역이 없습니다.").setTextAlignment(TextAlignment.CENTER)).setPadding(5).setBorder(new SolidBorder(0.5f)));
            } else {
                for (DailyReceiptLine line : purchaseLines) {
                    addDetailRow(purchaseTable, line);
                }
            }
            document.add(purchaseTable.setMarginBottom(20));

            // 6. 하단 푸터
            document.add(new Paragraph("\n본 문서는 시스템에 의해 자동 발행된 일일 정산 명세서이며, 정산 증빙용으로 사용 가능합니다.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));
            document.add(new Paragraph("(주)채잉 귀하").setBold().setTextAlignment(TextAlignment.RIGHT).setMarginTop(20));

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }


    @Override
    public byte[] createMonthlySettlementExcel(List<MonthlySettlement> settlements) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Monthly Settlements");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("가맹점 ID");
            header.createCell(1).setCellValue("정산월");
            header.createCell(2).setCellValue("총 매출액");
            header.createCell(3).setCellValue("발주 대금(-)");
            header.createCell(4).setCellValue("수수료 수익(-)");
            header.createCell(5).setCellValue("배송비(-)");
            header.createCell(6).setCellValue("반품 차감액(+)");
            header.createCell(7).setCellValue("본사 손실액(-)");
            header.createCell(8).setCellValue("최종 정산 금액");
            header.createCell(9).setCellValue("정산 상태");

            int rowIdx = 1;
            for (MonthlySettlement s : settlements) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getFranchiseId());
                row.createCell(1).setCellValue(s.getSettlementMonth().toString());
                row.createCell(2).setCellValue(s.getTotalSaleAmount().doubleValue());
                row.createCell(3).setCellValue(s.getOrderAmount().doubleValue());
                row.createCell(4).setCellValue(s.getCommissionFee().doubleValue());
                row.createCell(5).setCellValue(s.getDeliveryFee().doubleValue());
                row.createCell(6).setCellValue(s.getRefundAmount().doubleValue());
                row.createCell(7).setCellValue(s.getLossAmount().doubleValue());
                row.createCell(8).setCellValue(s.getFinalSettlementAmount().doubleValue());
                row.createCell(9).setCellValue(s.getStatus().name());
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Excel", e);
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    @Override
    public byte[] createMonthlyVoucherExcel(List<SettlementVoucher> vouchers) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Monthly Vouchers");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("발생일시");
            header.createCell(1).setCellValue("유형");
            header.createCell(2).setCellValue("설명");
            header.createCell(3).setCellValue("수량");
            header.createCell(4).setCellValue("단가");
            header.createCell(5).setCellValue("금액");
            header.createCell(6).setCellValue("참조코드");

            int rowIdx = 1;
            for (SettlementVoucher v : vouchers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(v.getOccurredAt().toString());
                row.createCell(1).setCellValue(v.getVoucherType().name());
                row.createCell(2).setCellValue(v.getDescription());
                row.createCell(3).setCellValue(v.getQuantity() != null ? v.getQuantity() : 0);
                row.createCell(4).setCellValue(v.getUnitPrice() != null ? v.getUnitPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(v.getAmount().doubleValue());
                row.createCell(6).setCellValue(v.getReferenceCode());
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Voucher Excel", e);
            throw new RuntimeException("Voucher Excel generation failed", e);
        }
    }

    @Override
    public byte[] createMonthlyReceiptPdf(MonthlySettlement settlement, List<SettlementVoucher> vouchers, String franchiseName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            PdfFont font = loadKoreanFont();
            document.setFont(font);
            document.setFontSize(9);

            // 1. 헤더
            document.add(new Paragraph("월간 정산 영수증 (공급증)")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));
            
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            metaTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("가맹점: " + franchiseName + " (ID: " + settlement.getFranchiseId() + ")")).setBorder(Border.NO_BORDER));
            metaTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("No. M-" + settlement.getMonthlySettlementId() + "\n정산월: " + settlement.getSettlementMonth())).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            document.add(metaTable.setMarginBottom(15));

            // 2. 공급자 정보
            Table providerTable = new Table(UnitValue.createPercentArray(new float[]{15, 35, 15, 35})).useAllAvailableWidth();
            addInfoCell(providerTable, "사업자번호", "123-45-67890", true);
            addInfoCell(providerTable, "상호", "(주)채잉 본사", false);
            addInfoCell(providerTable, "대표자", "김채잉", true);
            addInfoCell(providerTable, "주소", "서울 서초구 반포동 123", false);
            document.add(providerTable.setMarginBottom(20));

            // 3. 정산 요약
            document.add(new Paragraph("[ 정산 요약 ]").setBold().setMarginBottom(5));
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addSummaryRow(summaryTable, "총 매출액", settlement.getTotalSaleAmount());
            addSummaryRow(summaryTable, "반품 환급액 (+)", settlement.getRefundAmount());
            addSummaryRow(summaryTable, "발주 대금 (-)", settlement.getOrderAmount().negate());
            addSummaryRow(summaryTable, "정산 수수료 (-)", settlement.getCommissionFee().negate());
            addSummaryRow(summaryTable, "배송비 (-)", settlement.getDeliveryFee().negate());
            addSummaryRow(summaryTable, "본사 손실액 (-)", settlement.getLossAmount().negate());
            addSummaryRow(summaryTable, "기타 조정", settlement.getAdjustmentAmount());
            
            summaryTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("최종 정산 금액").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(5).setBorder(new SolidBorder(0.5f)));
            summaryTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(settlement.getFinalSettlementAmount())).setBold()).setTextAlignment(TextAlignment.RIGHT).setPadding(5).setBorder(new SolidBorder(0.5f)));
            document.add(summaryTable.setMarginBottom(25));

            // 4. 상세 내역 (Vouchers)
            document.add(new Paragraph("[ 정산 항목 상세 ]").setBold().setMarginBottom(5));
            float[] detailWidths = {20, 40, 10, 15, 15};
            Table details = new Table(UnitValue.createPercentArray(detailWidths)).useAllAvailableWidth();
            addDetailHeader(details, "일자");
            
            if (vouchers == null || vouchers.isEmpty()) {
                details.addCell(new com.itextpdf.layout.element.Cell(1, 5).add(new Paragraph("상세 내역이 없습니다.").setTextAlignment(TextAlignment.CENTER)).setPadding(5).setBorder(new SolidBorder(0.5f)));
            } else {
                for (SettlementVoucher v : vouchers) {
                    details.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(v.getOccurredAt().toString().substring(5, 10))).setFontSize(7).setPadding(3).setBorder(new SolidBorder(0.5f)));
                    details.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(v.getDescription())).setFontSize(7).setPadding(3).setBorder(new SolidBorder(0.5f)));
                    details.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(v.getQuantity() != null ? v.getQuantity().toString() : "1")).setFontSize(7).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
                    details.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(v.getUnitPrice() != null ? v.getUnitPrice() : v.getAmount()))).setFontSize(7).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
                    details.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(v.getAmount()))).setFontSize(7).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
                }
            }
            document.add(details);
            
            // 5. 하단 푸터
            document.add(new Paragraph("\n본 문서는 시스템에 의해 자동 발행된 월간 정산 명세서이며, 정산 증빙용으로 사용 가능합니다.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));
            document.add(new Paragraph("(주)채잉 귀하").setBold().setTextAlignment(TextAlignment.RIGHT).setMarginTop(20));

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Monthly PDF", e);
            throw new RuntimeException("Monthly PDF generation failed", e);
        }
    }

    private void addInfoCell(Table table, String label, String value, boolean isLabel) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(label).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(3).setBorder(new SolidBorder(0.5f)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(value))
                .setPadding(3).setBorder(new SolidBorder(0.5f)));
    }

    private void addSummaryRow(Table table, String label, BigDecimal amount) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(label)).setPadding(3).setBorder(new SolidBorder(0.5f)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(amount))).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
    }

    private void addDetailHeader(Table table, String firstColumnName) {
        String[] headers = {firstColumnName, "상품(항목)명", "수량", "단가", "금액"};
        for (String h : headers) {
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(h).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER).setPadding(3).setBorder(new SolidBorder(0.5f)));
        }
    }

    private void addDetailRow(Table table, DailyReceiptLine line) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(line.getOccurredAt().toString().substring(11, 16))).setFontSize(7).setPadding(3).setBorder(new SolidBorder(0.5f)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(line.getDescription())).setFontSize(7).setPadding(3).setBorder(new SolidBorder(0.5f)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(line.getQuantity() != null ? line.getQuantity().toString() : "1")).setFontSize(7).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(line.getUnitPrice() != null ? line.getUnitPrice() : line.getAmount()))).setFontSize(7).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(line.getAmount()))).setFontSize(7).setTextAlignment(TextAlignment.RIGHT).setPadding(3).setBorder(new SolidBorder(0.5f)));
    }


    @Override
    public byte[] createHQSettlementDailyPdf(java.time.LocalDate date, List<DailySettlementReceipt> receipts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. 폰트 로드 (실패 시 IllegalStateException 발생하여 즉시 중단)
            PdfFont font = loadKoreanFont();
            document.setFont(font);

            document.add(new Paragraph("HQ Daily Settlement Summary").setBold().setFontSize(20));
            document.add(new Paragraph("정산 일자: " + date));
            document.add(new Paragraph("총 가맹점 수: " + receipts.size()));
            document.add(new Paragraph("--------------------------------------------"));

            BigDecimal totalOrder = receipts.stream()
                    .map(r -> r.getOrderAmount() != null ? r.getOrderAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSale = receipts.stream()
                    .map(r -> r.getTotalSaleAmount() != null ? r.getTotalSaleAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = receipts.stream()
                    .map(r -> r.getCommissionFee() != null ? r.getCommissionFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivery = receipts.stream()
                    .map(r -> r.getDeliveryFee() != null ? r.getDeliveryFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRefund = receipts.stream()
                    .map(r -> r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLoss = receipts.stream()
                    .map(r -> r.getLossAmount() != null ? r.getLossAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal hqTotalFinal = totalOrder.add(totalFee).subtract(totalDelivery)
                    .subtract(totalRefund).subtract(totalLoss);

            document.add(new Paragraph("1. 전체 매출 합계(참고): " + totalSale + "원"));
            document.add(new Paragraph("2. 전체 발주 매출: " + totalOrder + "원"));
            document.add(new Paragraph("3. 전체 수수료 수익: " + totalFee + "원"));
            document.add(new Paragraph("4. 전체 배송비: -" + totalDelivery + "원"));
            document.add(new Paragraph("5. 전체 반품 차감액: -" + totalRefund + "원"));
            document.add(new Paragraph("6. 전체 본사 손실액: -" + totalLoss + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("전체 최종 정산 합계: " + hqTotalFinal + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[가맹점별 요약 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 80, 120, 120, 120 }));
            table.addCell("가맹점 ID");
            table.addCell("총 매출");
            table.addCell("수수료");
            table.addCell("정산금액");

            for (DailySettlementReceipt r : receipts) {
                BigDecimal order = r.getOrderAmount() != null ? r.getOrderAmount() : BigDecimal.ZERO;
                BigDecimal fee = r.getCommissionFee() != null ? r.getCommissionFee() : BigDecimal.ZERO;
                BigDecimal delivery = r.getDeliveryFee() != null ? r.getDeliveryFee() : BigDecimal.ZERO;
                BigDecimal refund = r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO;
                BigDecimal loss = r.getLossAmount() != null ? r.getLossAmount() : BigDecimal.ZERO;
                BigDecimal totalSaleRow = r.getTotalSaleAmount() != null ? r.getTotalSaleAmount() : BigDecimal.ZERO;

                BigDecimal hqAmount = order.add(fee).subtract(delivery).subtract(refund).subtract(loss);

                table.addCell(r.getFranchiseId().toString());
                table.addCell(totalSaleRow.toString() + "원");
                table.addCell(fee.toString() + "원");
                table.addCell(hqAmount.toString() + "원");
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate HQ Daily PDF", e);
            throw new RuntimeException("HQ Daily PDF generation failed", e);
        }
    }

    @Override
    public byte[] createHQSettlementMonthlyPdf(java.time.YearMonth month, List<MonthlySettlement> settlements) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. 폰트 로드 (실패 시 IllegalStateException 발생하여 즉시 중단)
            PdfFont font = loadKoreanFont();
            document.setFont(font);

            document.add(new Paragraph("HQ Monthly Settlement Summary").setBold().setFontSize(20));
            document.add(new Paragraph("정산 월: " + month));
            document.add(new Paragraph("총 가맹점 수: " + settlements.size()));
            document.add(new Paragraph("--------------------------------------------"));

            BigDecimal totalOrder = settlements.stream()
                    .map(s -> s.getOrderAmount() != null ? s.getOrderAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSale = settlements.stream()
                    .map(s -> s.getTotalSaleAmount() != null ? s.getTotalSaleAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = settlements.stream()
                    .map(s -> s.getCommissionFee() != null ? s.getCommissionFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivery = settlements.stream()
                    .map(s -> s.getDeliveryFee() != null ? s.getDeliveryFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRefund = settlements.stream()
                    .map(s -> s.getRefundAmount() != null ? s.getRefundAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLoss = settlements.stream()
                    .map(s -> s.getLossAmount() != null ? s.getLossAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal hqTotalFinal = totalOrder.add(totalFee).subtract(totalDelivery)
                    .subtract(totalRefund).subtract(totalLoss);

            document.add(new Paragraph("1. 전체 매출 합계(참고): " + totalSale + "원"));
            document.add(new Paragraph("2. 전체 발주 매출: " + totalOrder + "원"));
            document.add(new Paragraph("3. 전체 수수료 수익: " + totalFee + "원"));
            document.add(new Paragraph("4. 전체 배송비: -" + totalDelivery + "원"));
            document.add(new Paragraph("5. 전체 반품 차감액: -" + totalRefund + "원"));
            document.add(new Paragraph("6. 전체 본사 손실액: -" + totalLoss + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("전체 최종 정산 합계: " + hqTotalFinal + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[가맹점별 요약 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 80, 80, 80, 120, 100 }));
            table.addCell("가맹점 ID");
            table.addCell("총 매출");
            table.addCell("수수료");
            table.addCell("배송비(차감)");
            table.addCell("정산금액");

            for (MonthlySettlement s : settlements) {
                BigDecimal order = s.getOrderAmount() != null ? s.getOrderAmount() : BigDecimal.ZERO;
                BigDecimal fee = s.getCommissionFee() != null ? s.getCommissionFee() : BigDecimal.ZERO;
                BigDecimal delivery = s.getDeliveryFee() != null ? s.getDeliveryFee() : BigDecimal.ZERO;
                BigDecimal refund = s.getRefundAmount() != null ? s.getRefundAmount() : BigDecimal.ZERO;
                BigDecimal loss = s.getLossAmount() != null ? s.getLossAmount() : BigDecimal.ZERO;
                BigDecimal totalSaleRow = s.getTotalSaleAmount() != null ? s.getTotalSaleAmount() : BigDecimal.ZERO;

                BigDecimal hqAmount = order.add(fee).subtract(delivery).subtract(refund).subtract(loss);

                table.addCell(s.getFranchiseId().toString());
                table.addCell(totalSaleRow.toString() + "원");
                table.addCell(fee.toString() + "원");
                table.addCell(delivery.toString() + "원");
                table.addCell(hqAmount.toString() + "원");
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate HQ Monthly PDF", e);
            throw new RuntimeException("HQ Monthly PDF generation failed", e);
        }
    }

    private PdfFont loadKoreanFont() {
        String fontPath = "fonts/NanumGothic.ttf";
        try {
            log.info("[DEBUG] Attempting to load Korean font from classpath: {}", fontPath);
            ClassPathResource resource = new ClassPathResource(fontPath);

            if (!resource.exists()) {
                log.warn("[WARN] Font file NOT found in classpath: {}. Trying alternative locations...", fontPath);
                // 런타임 환경에 따라 /를 붙여야 할 수도 있음
                resource = new ClassPathResource("/" + fontPath);
            }

            if (!resource.exists()) {
                log.error("[ERROR] Font file NOT found in any known locations: {}", fontPath);
                // 마지막 수단: 기본 폰트로 폴백 (한글이 깨질 수 있지만 500 에러는 방지)
                return PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            }

            byte[] fontBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            log.info("[DEBUG] Font file loaded. Size: {} bytes", fontBytes.length);

            return PdfFontFactory.createFont(
                    fontBytes,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception e) {
            log.error("[CRITICAL] Failed to load Korean font '{}': {}. Falling back to standard HELVETICA.", fontPath, e.getMessage());
            try {
                return PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            } catch (IOException io) {
                throw new IllegalStateException("Critical failure: even StandardFonts cannot be loaded", io);
            }
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0원";
        // [수정] 소수점 절삭 방지를 위해 반올림(HALF_UP) 후 천 단위 콤마 포맷팅 적용
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(amount.setScale(0, java.math.RoundingMode.HALF_UP)) + "원";
    }
}
