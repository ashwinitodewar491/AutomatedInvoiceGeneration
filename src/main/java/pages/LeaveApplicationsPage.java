package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveApplicationsPage {

    private final Page page;

    public LeaveApplicationsPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(" Leave Applications")
        ).click();

        page.waitForURL("**/leave_applications");
    }

    public void applyFilters(String projectId, String from, String to) {
        page.locator("#project_id").selectOption(projectId);

        page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("From Date")
        ).fill(from);

        page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("To Date")
        ).fill(to);

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search")
        ).click();
    }

    public void openLeaveHistory() {
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Leave History")
        ).click();

        // initial table load
        page.waitForSelector("table tbody tr");
    }

    private boolean waitForTableRedraw(String previousFirstRowText) {
        try {
            page.waitForFunction(
                    "(prevText) => {" +
                            "const rows = document.querySelectorAll('table tbody tr');" +
                            "if (!rows || rows.length === 0) return false;" +
                            "return rows[0].innerText.trim() !== prevText.trim();" +
                            "}",
                    previousFirstRowText,
                    new Page.WaitForFunctionOptions().setTimeout(5000)
            );
            return true; // table changed
        } catch (PlaywrightException e) {
            System.out.println("No table change detected");
            return false;
        }
    }


    public Map<String, double[]> calculateLeaveDaysPerEmployee() {

        List<LeaveRow> tempRows = new ArrayList<>();
        int pageNo = 1;
        boolean hasNextPage = true;

        while (hasNextPage) {

            page.waitForSelector("table tbody tr");

            System.out.println("\n================ PAGE " + pageNo + " ================");

            // Fetching this just to check if user code is jumping to next page , for testing purpose only ,might remove later
            String firstRowSnapshot =
                    page.locator("table tbody tr").first().innerText();

            List<Locator> rows = page.locator("table tbody tr").all();

            System.out.println("Rows found: " + rows.size());

            for (int i = 0; i < rows.size(); i++) {

                String rowText = rows.get(i).innerText();
                System.out.println("Row " + (i + 1) + " -> " + rowText); // Will remove this after overall coding, this is for printing and checking purpose only

                List<Locator> cells = rows.get(i).locator("td").all();
                if (cells.size() < 9) continue;

                String employee  = cells.get(1).innerText().trim();
                String leaveType = cells.get(8).innerText().trim();
                String daysText  = cells.get(4).innerText().trim();

                if (leaveType.equalsIgnoreCase("WFH")) continue;

                try {
                    double days = Double.parseDouble(daysText);
                    tempRows.add(new LeaveRow(employee, days));
                } catch (Exception ignored) {}
            }

            // First scroll then click on next button as button is not visible to me util scroll
            Locator nextButton = page.getByRole(
                    AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Next")
            );
            if (nextButton.count() == 0) {
                hasNextPage = false;
                break;
            }
            // Scroll into view first
            nextButton.scrollIntoViewIfNeeded();

            // Small wait for UI stabilization as it might take sometimes load for loading data
            page.waitForTimeout(500);

            // Click only if enabled
            if (nextButton.isEnabled()) {
                nextButton.click();
                System.out.println("Clicked Next page");
            } else {
                System.out.println("Next button is disabled");
            }

            // WAIT until table content changes
            boolean changed = waitForTableRedraw(firstRowSnapshot);
            if (!changed) {
                System.out.println("No new page detected, stopping pagination");
                break;
            }

        }

        // Consolidate overall data
        Map<String, double[]> result = new HashMap<>();

        for (LeaveRow row : tempRows) {
            result.putIfAbsent(row.employee, new double[]{0, 0});
            result.get(row.employee)[0]++;      // transactions
            result.get(row.employee)[1] += row.days;
        }

        return result;
    }

}
