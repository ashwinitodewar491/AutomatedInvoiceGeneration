package tests;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;
import pages.LeaveApplicationsPage;
import pages.LoginPage2;
import pages.PendingLeaveRow;
import utils.EnvConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.LeaveReportExcelWriter;


public class LeaveReportTest {

    private Map<String, double[]> summarizePendingLeaves(
            List<PendingLeaveRow> pendingLeaves) {

        Map<String, double[]> summary = new HashMap<>();

        for (PendingLeaveRow row : pendingLeaves) {
            summary.putIfAbsent(row.employee, new double[]{0, 0});
            summary.get(row.employee)[0]++;        // transactions
            summary.get(row.employee)[1] += row.days; // total days
        }

        return summary;
    }
    @Test
    public void generateLeaveReport() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));

            Page page = browser.newPage();

            // LOGIN flow
            LoginPage2 login = new LoginPage2(page);
            login.login(EnvConfig.get("LOGIN_EMAIL"), EnvConfig.get("LOGIN_PASSWORD"));

            // OPEN LEAVE APPLICATIONS sub menu
            LeaveApplicationsPage leavePage = new LeaveApplicationsPage(page);
            leavePage.open();
            leavePage.applyFilters("445", "2024-01-13", "2024-07-13");

            leavePage.openLeaveHistory();

            // 🔹 GET SUMMARY ( calculate leave transaction and actual leave days , ignore WFH)
            Map<String, double[]> report =
                    leavePage.calculateLeaveDaysPerEmployee();

            System.out.println("\nEMPLOYEE LEAVE SUMMARY for leave history only");
            System.out.println("--------------------------------------------------");

            report.forEach((employee, data) -> {
                System.out.printf(
                        "%-20s | Transactions: %2d | Total Days: %5.2f%n",
                        employee,
                        (int) data[0],   // leave transaction count
                        data[1]          // total leave days available in leave transaction
                );
            });
            leavePage.openPendingLeaves();
            // FETCH PENDING DATA
            List<PendingLeaveRow> pendingLeaves =
                    leavePage.fetchPendingLeaves();

            System.out.println(
                    "---------------------------------------------------------------------");
            System.out.printf(
                    "%-20s %-12s %-12s %-6s %-12s %-30s%n",
                    "Employee", "Start Date", "End Date", "Days", "Type", "Reason"
            );
            System.out.println(
                    "----------------------------------------------------------------------");
            for (PendingLeaveRow row : pendingLeaves) {
                System.out.printf(
                        "%-20s %-12s %-12s %-6.1f %-12s %-30s%n",
                        row.employee,
                        row.startDate,
                        row.endDate,
                        row.days,
                        row.type,
                        row.reason
                );
            }
            System.out.println(
                    "-----------------------------------------------------------------------");
            Map<String, double[]> pendingSummary =
                    summarizePendingLeaves(pendingLeaves);

            System.out.println("\nEMPLOYEE LEAVE SUMMARY for PENDING leaves only");
            System.out.println("--------------------------------------------------");

            pendingSummary.forEach((employee, data) -> {
                System.out.printf(
                        "%-20s | Transactions: %2d | Total Days: %5.2f%n",
                        employee,
                        (int) data[0],
                        data[1]
                );
            });
            LeaveReportExcelWriter.generateExcel(report, pendingLeaves);
        }
    }
}