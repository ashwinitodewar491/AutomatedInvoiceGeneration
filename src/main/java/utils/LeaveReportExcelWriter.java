package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pages.PendingLeaveRow;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class LeaveReportExcelWriter {

    public static void generateExcel(
            Map<String, double[]> leaveHistorySummary,
            List<PendingLeaveRow> pendingLeaves
    ) {
        try (Workbook workbook = new XSSFWorkbook()) {

            // ================= SHEET 1 : Leave History =================
            Sheet historySheet = workbook.createSheet("Leave History Summary");

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
            Sheet pendingSheet = workbook.createSheet("Pending Leaves");

            Row pHeader = pendingSheet.createRow(0);
            pHeader.createCell(0).setCellValue("Employee");
            pHeader.createCell(1).setCellValue("Start Date");
            pHeader.createCell(2).setCellValue("Last Date");
            pHeader.createCell(3).setCellValue("Days");
            pHeader.createCell(4).setCellValue("reason");
            pHeader.createCell(5).setCellValue("Type");


            rowNum = 1;
            for (PendingLeaveRow row : pendingLeaves) {
                Row r = pendingSheet.createRow(rowNum++);
                r.createCell(0).setCellValue(row.employee);
                r.createCell(1).setCellValue(row.startDate);
                r.createCell(2).setCellValue(row.endDate);
                r.createCell(3).setCellValue(row.days);
                r.createCell(4).setCellValue(row.reason);
                r.createCell(5).setCellValue(row.type);

            }

            try (FileOutputStream fos =
                         new FileOutputStream("Leave_Report.xlsx")) {
                workbook.write(fos);
            }

            System.out.println("✅ Excel report generated: Leave_Report.xlsx");

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }
}
