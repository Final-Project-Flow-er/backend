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
    public byte[] createDailyReceiptPdf(DailySettlementReceipt receipt, List<DailyReceiptLine> lines) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. 폰트 로드 (실패 시 IllegalStateException 발생하여 즉시 중단)
            PdfFont font = loadKoreanFont();
            document.setFont(font);

            document.add(new Paragraph("Daily Settlement Receipt").setBold().setFontSize(20));
            document.add(new Paragraph("가맹점 ID: " + receipt.getFranchiseId()));
            document.add(new Paragraph("정산 일자: " + receipt.getSettlementDate()));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("1. 총 매출액: " + receipt.getTotalSaleAmount() + "원"));
            document.add(new Paragraph("2. 차감 내역:"));
            document.add(new Paragraph("   - 발주 대금: -" + receipt.getOrderAmount() + "원"));
            document.add(new Paragraph("   - 정산 수수료: -" + receipt.getCommissionFee() + "원"));
            document.add(new Paragraph("   - 배송비: -" + receipt.getDeliveryFee() + "원"));
            document.add(new Paragraph("   - 본사 손실액: -" + receipt.getLossAmount() + "원"));
            document.add(new Paragraph("3. 가산 내역:"));
            document.add(new Paragraph("   - 반품 환급액: +" + receipt.getRefundAmount() + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("최종 정산 금액: " + receipt.getFinalAmount() + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[상세 내역]")
                    .setFontSize(14)
                    .setBold());

            // 5개 컬럼: 유형, 상품/내역, 수량, 단가, 합계
            float[] columnWidths = { 80, 180, 50, 80, 100 };
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addCell("유형").setBold();
            table.addCell("상품/내역").setBold();
            table.addCell("수량").setBold();
            table.addCell("단가").setBold();
            table.addCell("합계").setBold();

            for (DailyReceiptLine line : lines) {
                table.addCell(line.getLineType().name());
                table.addCell(line.getDescription());
                table.addCell(line.getQuantity() != null ? line.getQuantity().toString() : "-");
                table.addCell(line.getUnitPrice() != null ? line.getUnitPrice().toString() + "원" : "-");
                table.addCell(line.getAmount().toString() + "원");
            }

            document.add(table);
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
    public byte[] createMonthlyReceiptPdf(MonthlySettlement settlement, List<SettlementVoucher> vouchers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. 폰트 로드 (실패 시 IllegalStateException 발생하여 즉시 중단)
            PdfFont font = loadKoreanFont();
            document.setFont(font);

            document.add(new Paragraph("Monthly Settlement Receipt").setBold().setFontSize(20));
            document.add(new Paragraph("가맹점 ID: " + settlement.getFranchiseId()));
            document.add(new Paragraph("정산 월: " + settlement.getSettlementMonth()));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("1. 총 매출액: " + settlement.getTotalSaleAmount() + "원"));
            document.add(new Paragraph("2. 차감 내역:"));
            document.add(new Paragraph("   - 발주 대금: -" + settlement.getOrderAmount() + "원"));
            document.add(new Paragraph("   - 정산 수수료: -" + settlement.getCommissionFee() + "원"));
            document.add(new Paragraph("   - 배송비: -" + settlement.getDeliveryFee() + "원"));
            document.add(new Paragraph("   - 본사 손실액: -" + settlement.getLossAmount() + "원"));
            document.add(new Paragraph("3. 가산 내역:"));
            document.add(new Paragraph("   - 반품 환급액: +" + settlement.getRefundAmount() + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("최종 정산 금액: " + settlement.getFinalSettlementAmount() + "원").setBold()
                    .setFontSize(14));

            document.add(new Paragraph("\n[상세 전표 목록]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 100, 80, 150, 100 }));
            table.addCell("날짜");
            table.addCell("유형");
            table.addCell("설명");
            table.addCell("금액");

            for (SettlementVoucher v : vouchers) {
                table.addCell(v.getOccurredAt().toLocalDate().toString());
                table.addCell(v.getVoucherType().name());
                table.addCell(v.getDescription());
                table.addCell(v.getAmount().toString() + "원");
            }

            document.add(table);
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
                    .map(DailySettlementReceipt::getOrderAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSale = receipts.stream()
                    .map(DailySettlementReceipt::getTotalSaleAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = receipts.stream()
                    .map(DailySettlementReceipt::getCommissionFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivery = receipts.stream()
                    .map(DailySettlementReceipt::getDeliveryFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRefund = receipts.stream()
                    .map(DailySettlementReceipt::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLoss = receipts.stream()
                    .map(DailySettlementReceipt::getLossAmount)
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
                BigDecimal hqAmount = r.getOrderAmount()
                        .add(r.getCommissionFee())
                        .add(r.getDeliveryFee())
                        .subtract(r.getRefundAmount())
                        .subtract(r.getLossAmount());

                table.addCell(r.getFranchiseId().toString());
                table.addCell(r.getTotalSaleAmount().toString() + "원");
                table.addCell(r.getCommissionFee().toString() + "원");
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
                    .map(MonthlySettlement::getOrderAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSale = settlements.stream()
                    .map(MonthlySettlement::getTotalSaleAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = settlements.stream()
                    .map(MonthlySettlement::getCommissionFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDelivery = settlements.stream()
                    .map(MonthlySettlement::getDeliveryFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRefund = settlements.stream()
                    .map(MonthlySettlement::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLoss = settlements.stream()
                    .map(MonthlySettlement::getLossAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal hqTotalFinal = totalOrder.add(totalFee).add(totalDelivery)
                    .subtract(totalRefund).subtract(totalLoss);

            document.add(new Paragraph("1. 전체 매출 합계(참고): " + totalSale + "원"));
            document.add(new Paragraph("2. 전체 발주 매출: " + totalOrder + "원"));
            document.add(new Paragraph("3. 전체 수수료 수익: " + totalFee + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("전체 최종 정산 합계: " + hqTotalFinal + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[가맹점별 요약 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 80, 150, 150 }));
            table.addCell("가맹점 ID");
            table.addCell("총 매출");
            table.addCell("정산금액");

            for (MonthlySettlement s : settlements) {
                BigDecimal hqAmount = s.getOrderAmount()
                        .add(s.getCommissionFee())
                        .add(s.getDeliveryFee())
                        .subtract(s.getRefundAmount())
                        .subtract(s.getLossAmount());

                table.addCell(s.getFranchiseId().toString());
                table.addCell(s.getTotalSaleAmount().toString() + "원");
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
                log.error("[ERROR] Font file NOT found in classpath: {}", fontPath);
                throw new java.io.IOException("Font file not found: " + fontPath);
            }

            byte[] fontBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            log.info("[DEBUG] Font file loaded. Size: {} bytes, Path: {}", fontBytes.length, fontPath);

            log.info("[DEBUG] Creating PdfFont with IDENTITY_H and PREFER_EMBEDDED");
            return PdfFontFactory.createFont(
                    fontBytes,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception e) {
            log.error("[CRITICAL] Failed to load Korean font '{}': {}", fontPath, e.getMessage());
            throw new IllegalStateException("한글 폰트 로드 실패: " + fontPath, e);
        }
    }
}
