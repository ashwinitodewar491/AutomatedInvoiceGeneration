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

            // LOGIN flow
            LoginPage2 login = new LoginPage2(page);
            login.login("pooja@joshsoftware.com", "josh123");

            // OPEN LEAVE APPLICATIONS sub menu
            LeaveApplicationsPage leavePage = new LeaveApplicationsPage(page);
            leavePage.open();
            leavePage.applyFilters("645", "2024-01-01", "2024-02-29");
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
        }
    }
}
