package com.chaing.api.service.settlement;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

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
}
