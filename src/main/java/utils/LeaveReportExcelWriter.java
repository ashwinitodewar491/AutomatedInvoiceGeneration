package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pages.PendingLeaveRow;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class LeaveReportExcelWriter {

    public static File generateExcel(
            Map<String, double[]> leaveHistorySummary,
            Map<String, double[]> pendingSummary,
            List<PendingLeaveRow> pendingLeaves
    ) {

        File excelFile = new File("Leave_Report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {

            // ================= SHEET 1 : Leave History =================
            Sheet historySheet = workbook.createSheet("Approved Leave History");

            Row header = historySheet.createRow(0);
            header.createCell(0).setCellValue("Employee");
            header.createCell(1).setCellValue("Transactions");
            header.createCell(2).setCellValue("Total Leave Days");

            int rowNum = 1;
            for (Map.Entry<String, double[]> entry : leaveHistorySummary.entrySet()) {
                Row row = historySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue((int) entry.getValue()[0]);
                row.createCell(2).setCellValue(entry.getValue()[1]);
            }

            // ================= SHEET 2 : Pending Leaves =================
            Sheet pendingSheet = workbook.createSheet("Pending Leaves History");

            rowNum = 0;

            // -------- TITLE --------
            Row title = pendingSheet.createRow(rowNum++);
            title.createCell(0).setCellValue("EMPLOYEE PENDING LEAVE SUMMARY (Contains WFH also) ");

            // -------- SUMMARY HEADER --------
            Row summaryHeader = pendingSheet.createRow(rowNum++);
            summaryHeader.createCell(0).setCellValue("Employee");
            summaryHeader.createCell(1).setCellValue("Transactions");
            summaryHeader.createCell(2).setCellValue("Total Days");

            // -------- SUMMARY DATA
            for (Map.Entry<String, double[]> entry : pendingSummary.entrySet()) {
                Row r = pendingSheet.createRow(rowNum++);
                r.createCell(0).setCellValue(entry.getKey());
                r.createCell(1).setCellValue((int) entry.getValue()[0]);
                r.createCell(2).setCellValue(entry.getValue()[1]);
            }

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }

            System.out.println("✅ Excel report generated: " + excelFile.getAbsolutePath());
            return excelFile;

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }
}
