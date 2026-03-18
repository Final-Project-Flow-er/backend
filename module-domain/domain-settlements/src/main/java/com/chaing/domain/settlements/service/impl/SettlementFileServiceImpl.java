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
import com.itextpdf.kernel.colors.DeviceRgb;
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
            document.setMargins(40, 40, 40, 40);

            PdfFont font = loadKoreanFont();
            document.setFont(font);

            // 색상 정의
            DeviceRgb navy = new DeviceRgb(0, 21, 41);
            DeviceRgb blueGray = new DeviceRgb(69, 90, 100);
            DeviceRgb lightGray = new DeviceRgb(240, 242, 245);

            // 1. 헤더 영역
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            headerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("DAILY SETTLEMENT REPORT")
                    .setBold().setFontSize(24).setFontColor(navy)).setBorder(Border.NO_BORDER).setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));
            headerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(franchiseName + "\n" + "ID: " + receipt.getFranchiseId())
                    .setFontSize(10).setFontColor(blueGray).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            document.add(headerTable);
            
            document.add(new Paragraph("정산 일자: " + receipt.getSettlementDate().toString())
                    .setFontSize(10).setFontColor(blueGray).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));

            // 2. 요약 정산 섹션 (강조 박스)
            Table summaryBox = new Table(1).useAllAvailableWidth();
            summaryBox.setMarginBottom(30);
            com.itextpdf.layout.element.Cell summaryCell = new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(lightGray).setPadding(20).setBorder(new SolidBorder(navy, 1f));
            
            summaryCell.add(new Paragraph("최종 정산 금액").setFontSize(12).setFontColor(navy).setBold().setTextAlignment(TextAlignment.CENTER));
            summaryCell.add(new Paragraph(formatCurrency(receipt.getFinalAmount()))
                    .setFontSize(32).setBold().setFontColor(navy).setTextAlignment(TextAlignment.CENTER));
            
            summaryBox.addCell(summaryCell);
            document.add(summaryBox);

            // 3. 상세 항목 리스트
            document.add(new Paragraph("정산 상세 내역").setBold().setFontSize(14).setFontColor(navy).setMarginBottom(10));
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            itemTable.setMarginBottom(20);

            addReportRow(itemTable, "총 매출액", receipt.getTotalSaleAmount(), false, navy, blueGray);
            addReportRow(itemTable, "반품 환급액", receipt.getRefundAmount(), false, navy, blueGray);
            addReportRow(itemTable, "발주 대금", receipt.getOrderAmount(), true, navy, blueGray);
            addReportRow(itemTable, "정산 수수료", receipt.getCommissionFee(), true, navy, blueGray);
            addReportRow(itemTable, "배송비", receipt.getDeliveryFee(), true, navy, blueGray);
            addReportRow(itemTable, "본사 손실액", receipt.getLossAmount(), true, navy, blueGray);
            addReportRow(itemTable, "기타 조정", receipt.getAdjustmentAmount(), false, navy, blueGray);

            document.add(itemTable);

            // 4. 상품별 판매 내역
            if (lines != null && !lines.isEmpty()) {
                document.add(new Paragraph("상품별 상세 내역").setBold().setFontSize(14).setFontColor(navy).setMarginTop(20).setMarginBottom(10));
                Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
                for (DailyReceiptLine line : lines) {
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(line.getDescription()).setFontSize(9).setFontColor(blueGray)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(line.getAmount())).setFontSize(9).setFontColor(blueGray)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
                }
                document.add(table);
            }

            document.add(new Paragraph("\n\n본 명세서는 시스템에 의해 자동 생성된 월간 정산 공식 문서입니다.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));

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
            header.createCell(5).setCellValue("배송 수익(-)");
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
            document.setMargins(40, 40, 40, 40);

            PdfFont font = loadKoreanFont();
            document.setFont(font);

            // 색상 정의
            DeviceRgb navy = new DeviceRgb(0, 21, 41);
            DeviceRgb blueGray = new DeviceRgb(69, 90, 100);
            DeviceRgb lightGray = new DeviceRgb(240, 242, 245);

            // 1. 헤더 영역
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            headerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("MONTHLY SETTLEMENT REPORT")
                    .setBold().setFontSize(24).setFontColor(navy)).setBorder(Border.NO_BORDER).setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));
            headerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(franchiseName + "\n" + "ID: " + settlement.getFranchiseId())
                    .setFontSize(10).setFontColor(blueGray).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            document.add(headerTable);
            
            document.add(new Paragraph("정산 월: " + settlement.getSettlementMonth().toString())
                    .setFontSize(10).setFontColor(blueGray).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));

            // 2. 요약 정산 섹션 (강조 박스)
            Table summaryBox = new Table(1).useAllAvailableWidth();
            summaryBox.setMarginBottom(30);
            com.itextpdf.layout.element.Cell summaryCell = new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(lightGray).setPadding(20).setBorder(new SolidBorder(navy, 1f));
            
            summaryCell.add(new Paragraph("당월 최종 정산 금액").setFontSize(12).setFontColor(navy).setBold().setTextAlignment(TextAlignment.CENTER));
            summaryCell.add(new Paragraph(formatCurrency(settlement.getFinalSettlementAmount()))
                    .setFontSize(32).setBold().setFontColor(navy).setTextAlignment(TextAlignment.CENTER));
            
            summaryBox.addCell(summaryCell);
            document.add(summaryBox);

            // 3. 상세 항목 리스트
            document.add(new Paragraph("정산 상세 내역").setBold().setFontSize(14).setFontColor(navy).setMarginBottom(10));
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            itemTable.setMarginBottom(20);

            addReportRow(itemTable, "총 매출액", settlement.getTotalSaleAmount(), false, navy, blueGray);
            addReportRow(itemTable, "반품 환급액", settlement.getRefundAmount(), false, navy, blueGray);
            addReportRow(itemTable, "발주 대금", settlement.getOrderAmount(), true, navy, blueGray);
            addReportRow(itemTable, "정산 수수료", settlement.getCommissionFee(), true, navy, blueGray);
            addReportRow(itemTable, "배송비", settlement.getDeliveryFee(), true, navy, blueGray);
            addReportRow(itemTable, "본사 손실액", settlement.getLossAmount(), true, navy, blueGray);
            addReportRow(itemTable, "기타 조정", settlement.getAdjustmentAmount(), false, navy, blueGray);

            document.add(itemTable);

            document.add(new Paragraph("\n\n본 명세서는 시스템에 의해 자동 생성된 월간 정산 공식 문서입니다.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Monthly PDF", e);
            throw new RuntimeException("Monthly PDF generation failed", e);
        }
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

            BigDecimal hqTotalFinal = totalOrder.add(totalFee).add(totalDelivery)
                    .subtract(totalRefund).subtract(totalLoss);

            document.add(new Paragraph("1. 전체 매출 합계(참고): " + totalSale + "원"));
            document.add(new Paragraph("2. 전체 발주 매출: " + totalOrder + "원"));
            document.add(new Paragraph("3. 전체 수수료 수익: " + totalFee + "원"));
            document.add(new Paragraph("4. 전체 배송 수익: " + totalDelivery + "원"));
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

                BigDecimal hqAmount = order.add(fee).add(delivery).subtract(refund).subtract(loss);

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

            BigDecimal hqTotalFinal = totalOrder.add(totalFee).add(totalDelivery)
                    .subtract(totalRefund).subtract(totalLoss);

            document.add(new Paragraph("1. 전체 매출 합계(참고): " + totalSale + "원"));
            document.add(new Paragraph("2. 전체 발주 매출: " + totalOrder + "원"));
            document.add(new Paragraph("3. 전체 수수료 수익: " + totalFee + "원"));
            document.add(new Paragraph("4. 전체 배송 수익: " + totalDelivery + "원"));
            document.add(new Paragraph("5. 전체 반품 차감액: -" + totalRefund + "원"));
            document.add(new Paragraph("6. 전체 본사 손실액: -" + totalLoss + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("전체 최종 정산 합계: " + hqTotalFinal + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[가맹점별 요약 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 80, 80, 80, 120, 100 }));
            table.addCell("가맹점 ID");
            table.addCell("총 매출");
            table.addCell("수수료");
            table.addCell("기타(배송/반품/손실)");
            table.addCell("정산금액");

            for (MonthlySettlement s : settlements) {
                BigDecimal order = s.getOrderAmount() != null ? s.getOrderAmount() : BigDecimal.ZERO;
                BigDecimal fee = s.getCommissionFee() != null ? s.getCommissionFee() : BigDecimal.ZERO;
                BigDecimal delivery = s.getDeliveryFee() != null ? s.getDeliveryFee() : BigDecimal.ZERO;
                BigDecimal refund = s.getRefundAmount() != null ? s.getRefundAmount() : BigDecimal.ZERO;
                BigDecimal loss = s.getLossAmount() != null ? s.getLossAmount() : BigDecimal.ZERO;
                BigDecimal totalSaleRow = s.getTotalSaleAmount() != null ? s.getTotalSaleAmount() : BigDecimal.ZERO;

                BigDecimal otherAmount = delivery.subtract(refund).subtract(loss);
                BigDecimal hqAmount = order.add(fee).add(otherAmount);

                table.addCell(s.getFranchiseId().toString());
                table.addCell(totalSaleRow.toString() + "원");
                table.addCell(fee.toString() + "원");
                table.addCell(otherAmount.toString() + "원");
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

    private void addReportRow(Table table, String label, BigDecimal amount, boolean isDeduction, DeviceRgb navy, DeviceRgb blueGray) {
        if (amount == null) amount = BigDecimal.ZERO;
        
        // 0원인 경우 흐리게 처리
        com.itextpdf.kernel.colors.Color fontColor = (amount.compareTo(BigDecimal.ZERO) == 0) ? ColorConstants.LIGHT_GRAY : blueGray;
        
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(label).setFontSize(10).setFontColor(fontColor)).setBorder(Border.NO_BORDER).setPadding(5));
        
        String sign = "";
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            if (isDeduction) sign = "-";
            else if (amount.compareTo(BigDecimal.ZERO) > 0) sign = "+";
        }
        
        String value = sign + formatCurrency(amount.abs());
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(value).setFontSize(10).setBold().setFontColor(fontColor))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPadding(5));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0원";
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(amount.longValue()) + "원";
    }
}
