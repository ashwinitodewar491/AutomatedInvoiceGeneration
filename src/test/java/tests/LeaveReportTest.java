package tests;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;
import pages.LeaveApplicationsPage;
import pages.LoginPage2;
import pages.PendingLeaveRow;
import utils.EmailUtil;
import utils.EnvConfig;
import java.io.File;
import java.util.ArrayList;
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
            summary.get(row.employee)[0]++;
            summary.get(row.employee)[1] += row.days;
        }
        return summary;
    }

    private String[] getCurrentMonthRange() {

        LocalDate now = LocalDate.now();
        LocalDate fromDate = now.withDayOfMonth(1);
        LocalDate toDate = now.with(TemporalAdjusters.lastDayOfMonth());

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new String[]{
                fromDate.format(formatter),
                toDate.format(formatter)
        };
    }

    @Test
    public void generateLeaveReport() {

        String projectIdsProp = System.getProperty("PROJECT_ID", "445,645");
        if (projectIdsProp.isBlank()) {
            throw new IllegalStateException(
                    "PROJECT_ID is required. Example: -DPROJECT_ID=445,645"
            );
        }
        String[] projectIds = projectIdsProp.split(",");

        // ✅ Collect all excel files here
        List<File> attachments = new ArrayList<>();
        StringBuilder projectListForMail = new StringBuilder();

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));

            Page page = browser.newPage();

            // LOGIN flow (ONLY ONCE)
            LoginPage2 login = new LoginPage2(page);
            login.login(
                    EnvConfig.get("LOGIN_EMAIL"),
                    EnvConfig.get("LOGIN_PASSWORD")
            );

            LeaveApplicationsPage leavePage = new LeaveApplicationsPage(page);
            leavePage.open();

            //String[] dateRange = getCurrentMonthRange();

            // ================= LOOP OVER PROJECTS =================
            for (String projectId : projectIds) {

                projectId = projectId.trim();
                System.out.println("Processing projectId = " + projectId);

//                String projectName = leavePage.applyFilters(
//                        projectId,
//                        dateRange[0],
//                        dateRange[1]
//                );
                String projectName=leavePage.applyFilters(projectId, "2024-01-13", "2024-02-13"); //Will keep this for testing purpose
//                System.out.println(
//                        "Date range: From = " + dateRange[0] +
//                                ", To = " + dateRange[1]
//                );

                leavePage.openLeaveHistory();

                Map<String, double[]> report =
                        leavePage.calculateLeaveDaysPerEmployee();

                leavePage.openPendingLeaves();

                List<PendingLeaveRow> pendingLeaves =
                        leavePage.fetchPendingLeaves();

                Map<String, double[]> pendingSummary =
                        summarizePendingLeaves(pendingLeaves);

                File excel = LeaveReportExcelWriter.generateExcel(
                        projectName,
                        report,
                        pendingSummary,
                        pendingLeaves
                );

                // ✅ STORE FILE (DON'T SEND EMAIL HERE)
                attachments.add(excel);
                projectListForMail.append("• ").append(projectName).append("\n");
            }

            // ================= SEND SINGLE EMAIL =================
            if (!attachments.isEmpty()) {
                String finalSubject = "Monthly Leave Report (Multiple Projects)";
                String finalMailContent =
                        "Hello Team,\n\n" +
                                "Please find attached the monthly leave reports for the following projects:\n\n" +
                                projectListForMail +
                                "\nRegards,\nAutomated Mailer Josh";

                EmailUtil.sendEmailWithMultipleAttachments(
                        attachments,
                        "ashwini.todewar@joshsoftware.com",
                        finalSubject,
                        finalMailContent
                );
            }
        }
    }
}
