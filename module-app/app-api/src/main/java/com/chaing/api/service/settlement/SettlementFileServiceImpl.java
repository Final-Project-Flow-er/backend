package com.chaing.api.service.settlement;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.entity.SettlementVoucher;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
            PdfFont font = getKoreanFont();
            if (font != null) {
                document.setFont(font);
            }

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

            document.add(new Paragraph("\n[상세 전표 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 100, 200, 100 }));
            table.addCell("유형");
            table.addCell("설명");
            table.addCell("금액");

            for (DailyReceiptLine line : lines) {
                table.addCell(line.getLineType().name());
                table.addCell(line.getDescription());
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
    public byte[] createMonthlyReceiptPdf(com.chaing.domain.settlements.entity.MonthlySettlement settlement,
            List<com.chaing.domain.settlements.entity.SettlementVoucher> vouchers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);
            PdfFont font = getKoreanFont();
            if (font != null) {
                document.setFont(font);
            }

            document.add(
                    new com.itextpdf.layout.element.Paragraph("Monthly Settlement Receipt").setBold().setFontSize(20));
            document.add(new com.itextpdf.layout.element.Paragraph("가맹점 ID: " + settlement.getFranchiseId()));
            document.add(new com.itextpdf.layout.element.Paragraph("정산 월: " + settlement.getSettlementMonth()));
            document.add(new com.itextpdf.layout.element.Paragraph("--------------------------------------------"));
            document.add(
                    new com.itextpdf.layout.element.Paragraph("1. 총 매출액: " + settlement.getTotalSaleAmount() + "원"));
            document.add(new com.itextpdf.layout.element.Paragraph("2. 차감 내역:"));
            document.add(
                    new com.itextpdf.layout.element.Paragraph("   - 발주 대금: -" + settlement.getOrderAmount() + "원"));
            document.add(
                    new com.itextpdf.layout.element.Paragraph("   - 정산 수수료: -" + settlement.getCommissionFee() + "원"));
            document.add(new com.itextpdf.layout.element.Paragraph("   - 배송비: -" + settlement.getDeliveryFee() + "원"));
            document.add(
                    new com.itextpdf.layout.element.Paragraph("   - 본사 손실액: -" + settlement.getLossAmount() + "원"));
            document.add(new com.itextpdf.layout.element.Paragraph("3. 가산 내역:"));
            document.add(
                    new com.itextpdf.layout.element.Paragraph("   - 반품 환급액: +" + settlement.getRefundAmount() + "원"));
            document.add(new com.itextpdf.layout.element.Paragraph("--------------------------------------------"));
            document.add(new com.itextpdf.layout.element.Paragraph(
                    "최종 정산 금액: " + settlement.getFinalSettlementAmount() + "원").setBold().setFontSize(14));

            document.add(new com.itextpdf.layout.element.Paragraph("\n[상세 전표 목록]"));
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
                    com.itextpdf.layout.properties.UnitValue.createPointArray(new float[] { 100, 80, 150, 100 }));
            table.addCell("날짜");
            table.addCell("유형");
            table.addCell("설명");
            table.addCell("금액");

            for (com.chaing.domain.settlements.entity.SettlementVoucher v : vouchers) {
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
            PdfFont font = getKoreanFont();
            if (font != null) {
                document.setFont(font);
            }

            document.add(new Paragraph("HQ Daily Settlement Summary").setBold().setFontSize(20));
            document.add(new Paragraph("정산 일자: " + date));
            document.add(new Paragraph("총 가맹점 수: " + receipts.size()));
            document.add(new Paragraph("--------------------------------------------"));

            BigDecimal totalFinal = receipts.stream()
                    .map(DailySettlementReceipt::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSale = receipts.stream()
                    .map(DailySettlementReceipt::getTotalSaleAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = receipts.stream()
                    .map(DailySettlementReceipt::getCommissionFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            document.add(new Paragraph("1. 전체 매출 합계: " + totalSale + "원"));
            document.add(new Paragraph("2. 전체 수수료 수익: " + totalFee + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("전체 최종 정산 합계: " + totalFinal + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[가맹점별 요약 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 80, 120, 120, 120 }));
            table.addCell("가맹점 ID");
            table.addCell("총 매출");
            table.addCell("수수료");
            table.addCell("정산금액");

            for (DailySettlementReceipt r : receipts) {
                table.addCell(r.getFranchiseId().toString());
                table.addCell(r.getTotalSaleAmount().toString() + "원");
                table.addCell(r.getCommissionFee().toString() + "원");
                table.addCell(r.getFinalAmount().toString() + "원");
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
            PdfFont font = getKoreanFont();
            if (font != null) {
                document.setFont(font);
            }

            document.add(new Paragraph("HQ Monthly Settlement Summary").setBold().setFontSize(20));
            document.add(new Paragraph("정산 월: " + month));
            document.add(new Paragraph("총 가맹점 수: " + settlements.size()));
            document.add(new Paragraph("--------------------------------------------"));

            BigDecimal totalFinal = settlements.stream()
                    .map(MonthlySettlement::getFinalSettlementAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSale = settlements.stream()
                    .map(MonthlySettlement::getTotalSaleAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            document.add(new Paragraph("1. 전체 매출 합계: " + totalSale + "원"));
            document.add(new Paragraph("--------------------------------------------"));
            document.add(new Paragraph("전체 최종 정산 합계: " + totalFinal + "원").setBold().setFontSize(14));

            document.add(new Paragraph("\n[가맹점별 요약 내역]"));
            Table table = new Table(UnitValue.createPointArray(new float[] { 80, 150, 150 }));
            table.addCell("가맹점 ID");
            table.addCell("총 매출");
            table.addCell("정산금액");

            for (MonthlySettlement s : settlements) {
                table.addCell(s.getFranchiseId().toString());
                table.addCell(s.getTotalSaleAmount().toString() + "원");
                table.addCell(s.getFinalSettlementAmount().toString() + "원");
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate HQ Monthly PDF", e);
            throw new RuntimeException("HQ Monthly PDF generation failed", e);
        }
    }

    private PdfFont getKoreanFont() {
        // 탐색할 한글 폰트 경로 목록 (Docker, Linux, Mac 대응)
        String[] fontPaths = {
                // Mac
                "/System/Library/Fonts/Supplemental/AppleGothic.ttf",
                "/Library/Fonts/AppleGothic.ttf",
                "/System/Library/Fonts/AppleSDGothicNeo.ttc",
                // Linux (Common)
                "/usr/share/fonts/truetype/nanum/NanumGothic.ttf",
                "/usr/share/fonts/nanum/NanumGothic.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/noto-cjk/NotoSansCJK-Regular.ttc"
        };

        for (String path : fontPaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    log.info("Loading Korean font from: {}", path);
                    // ttc(TrueType Collection)의 경우 첫 번째 인덱스(0)를 사용하도록 처리할 수 있음
                    if (path.endsWith(".ttc")) {
                        return PdfFontFactory.createFont(path + ",0", PdfEncodings.IDENTITY_H);
                    }
                    return PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H);
                }
            } catch (Exception e) {
                log.warn("Failed to load font from {}: {}", path, e.getMessage());
            }
        }

        log.error("No Korean font found in system paths. PDF might show boxes instead of Korean text.");
        return null; // font가 null이면 iText는 기본 폰트(English only)를 사용합니다.
    }
}
