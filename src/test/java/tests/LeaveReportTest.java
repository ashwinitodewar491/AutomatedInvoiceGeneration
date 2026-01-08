package tests;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;
import pages.LeaveApplicationsPage;
import pages.LoginPage2;

import java.util.Map;

public class LeaveReportTest {

    @Test
    public void generateLeaveReport() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));

            Page page = browser.newPage();

            // LOGIN
            LoginPage2 login = new LoginPage2(page);
            login.login("pooja@joshsoftware.com", "josh123");

            // OPEN LEAVE APPLICATIONS
            LeaveApplicationsPage leavePage = new LeaveApplicationsPage(page);
            leavePage.open();
            leavePage.applyFilters("645", "2024-01-01", "2024-02-29");
            leavePage.openLeaveHistory();

            // 🔹 GET SUMMARY
            Map<String, int[]> report =
                    leavePage.calculateLeaveDaysPerEmployee();

            System.out.println("\nEMPLOYEE LEAVE SUMMARY");
            System.out.println("-------------------------------------------");

            report.forEach((employee, data) -> {
                System.out.printf(
                        "%-20s | Transactions: %2d | Total Days: %2d%n",
                        employee,
                        data[0],
                        data[1]
        );
            });
    }}}

