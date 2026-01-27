package tests;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;
import pages.LeaveApplicationsPage;
import pages.LoginPage2;
import pages.PendingLeaveRow;
import utils.EmailUtil;
import utils.EnvConfig;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.LeaveReportExcelWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;


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
    private String[] getCurrentMonthRange() {

        LocalDate now = LocalDate.now();

        LocalDate fromDate = now.withDayOfMonth(1);
        LocalDate toDate   = now.with(TemporalAdjusters.lastDayOfMonth());

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new String[]{
                fromDate.format(formatter),
                toDate.format(formatter)
        };
    }
    @Test
    public void generateLeaveReport() {
        String projectId = System.getProperty("PROJECT_ID","445");

        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "❌ projectId is required. Pass it using -DPROJECT_ID=XXX"
            );
        }
        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));

            Page page = browser.newPage();

            // LOGIN flow
            LoginPage2 login = new LoginPage2(page);
            login.login(EnvConfig.get("LOGIN_EMAIL"), EnvConfig.get("LOGIN_PASSWORD"));

            // OPEN LEAVE APPLICATIONS sub menu
            LeaveApplicationsPage leavePage = new LeaveApplicationsPage(page);
            leavePage.open();
            //String projectName=leavePage.applyFilters(projectId, "2024-01-13", "2024-07-13"); //Will keep this for testing purpose
            String[] dateRange = getCurrentMonthRange();
            String projectName=leavePage.applyFilters(projectId, dateRange[0], dateRange[1]);
            System.out.println(
                    "Date range getting passed: From = " + dateRange[0] +
                            ", To = " + dateRange[1]
            );
            String subject = "Monthly Leave Report : "+ projectName;
            String mailContent =
                    "Hello Team,\n\n" +
                            "Please find attached the leave report.\n\n" +
                            "Project: " + projectName + "\n";
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
                    "----------------------------------------------------------------------");
            for (PendingLeaveRow row : pendingLeaves) {
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

            File excel = LeaveReportExcelWriter.generateExcel(
                    projectName,
                    report,
                    pendingSummary,
                    pendingLeaves
            );

            EmailUtil.sendEmailWithAttachment( excel,
                    "ashwini.todewar@joshsoftware.com",subject,
                    projectName,
                    mailContent
                     );
        }
    }
}