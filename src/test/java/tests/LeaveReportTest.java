package tests;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;
import pages.LeaveApplicationsPage;
import pages.LoginPage2;
import models.PendingLeaveRow;
import services.LeaveReportService;
import utils.DateUtil;
import utils.EmailUtil;
import utils.EnvConfig;
import utils.LeaveReportExcelWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaveReportTest {

    @Test
    public void generateLeaveReport() {

        String[] projectIds =
                System.getProperty("PROJECT_ID", "445,284").split(",");

        List<File> attachments = new ArrayList<>();
        StringBuilder projectNames = new StringBuilder();

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));

            Page page = browser.newPage();

            new LoginPage2(page).login(
                    EnvConfig.get("LOGIN_EMAIL"),
                    EnvConfig.get("LOGIN_PASSWORD")
            );

            LeaveApplicationsPage leavePage = new LeaveApplicationsPage(page);
            LeaveReportService service = new LeaveReportService(leavePage);

            leavePage.open();

            String[] range = DateUtil.getCurrentMonthRange();

            boolean atLeastOneValidProject = false;

            for (String rawProjectId : projectIds) {

                String projectId = rawProjectId.trim();

                // 1️⃣ Empty / comma-only case
                if (projectId.isEmpty()) {
                    System.out.println("Skipping empty projectId");
                    continue;
                }

                // 2️⃣ Validate project exists
                if (!leavePage.isProjectIdPresent(projectId)) {
                    System.out.println(
                            "Invalid projectId (not available): " + projectId
                    );
                    continue;
                }

                atLeastOneValidProject = true;

                System.out.println("Processing projectId: " + projectId);

//                String projectName = leavePage.applyFilters(
//                        projectId, range[0], range[1]
//                );
                String projectName=leavePage.applyFilters(projectId, "2024-01-13", "2024-07-13"); //Will keep this for testing purpose


                leavePage.openLeaveHistory();

                Map<String, double[]> historySummary =
                        service.getLeaveHistorySummary();

                leavePage.openPendingLeaves();

                List<PendingLeaveRow> pendingLeaves =
                        leavePage.fetchPendingLeaves();

                Map<String, double[]> pendingSummary =
                        service.getPendingSummary(pendingLeaves);

                File excel = LeaveReportExcelWriter.generateExcel(
                        projectName,
                        historySummary,
                        pendingSummary,
                        pendingLeaves
                );

                attachments.add(excel);
                projectNames.append("• ").append(projectName).append("\n");
            }
            if (!atLeastOneValidProject) {
                throw new IllegalStateException(
                        "No valid projectId found. Please check PROJECT_ID parameter."
                );
            }

            EmailUtil.sendEmailWithMultipleAttachments(
                    attachments,
                    EnvConfig.get("LOGIN_EMAIL_RECIPIENT"),
                    "Monthly Leave Report",
                    "Hello Team,\n\nPlease find attached reports for:\n\n"
                            + projectNames +
                            "\nRegards,\nAutomation"
            );
        }
    }
}
