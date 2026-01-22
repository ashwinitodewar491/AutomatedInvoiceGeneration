package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

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
        //page.waitForSelector("table tbody tr");
        page.waitForFunction("""
        () => {
            const table = document.querySelector('table tbody');
            if (!table) return false;

            // Either rows exist OR empty message is shown
            return table.querySelectorAll('tr').length >= 0;
        }
    """);
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
        boolean hasNextPage = true;

        while (hasNextPage) {

            // 1️⃣ Wait only for table body (not rows)
            page.waitForFunction("""
            () => document.querySelector('table tbody') !== null
        """);

            Locator rowsLocator = page.locator("table tbody tr");
            int rowCount = rowsLocator.count();

            // 2️⃣ ZERO-ROW GUARD (EXIT EARLY)
            if (rowCount == 0) {
                System.out.println("⚠️ No leave history rows found. Skipping row processing.");
                break;
            }

            // 3️⃣ SAFE snapshot (only when rows exist)
            String firstRowSnapshot = rowsLocator.first().innerText();

            // 4️⃣ Read rows ONLY when rowCount > 0
            for (int i = 0; i < rowCount; i++) {

                Locator row = rowsLocator.nth(i);
                List<Locator> cells = row.locator("td").all();
                if (cells.size() < 9) continue;

                String employee     = cells.get(1).innerText().trim();
                String leaveType    = cells.get(8).innerText().trim();
                String daysText     = cells.get(4).innerText().trim();
                String leaveStatus  = cells.get(7).innerText().trim();

                if (leaveType.equalsIgnoreCase("WFH")) continue;
                if (leaveStatus.equalsIgnoreCase("Rejected")) continue;

                try {
                    double days = Double.parseDouble(daysText);
                    tempRows.add(new LeaveRow(employee, days));
                } catch (Exception ignored) {}
            }

            // 5️⃣ Pagination logic
            Locator nextButton = page.getByRole(
                    AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Next")
            );

            if (nextButton.count() == 0 || !nextButton.isEnabled()) {
                hasNextPage = false;
                break;
            }

            nextButton.scrollIntoViewIfNeeded();
            page.waitForTimeout(500);
            nextButton.click();

            boolean changed = waitForTableRedraw(firstRowSnapshot);
            if (!changed) break;
        }

        // 6️⃣ Consolidation
        Map<String, double[]> result = new HashMap<>();
        for (LeaveRow row : tempRows) {
            result.putIfAbsent(row.employee, new double[]{0, 0});
            result.get(row.employee)[0]++;
            result.get(row.employee)[1] += row.days;
        }

        return result;
    }


    public List<PendingLeaveRow> fetchPendingLeaves() {

        page.waitForLoadState(LoadState.NETWORKIDLE);

        List<PendingLeaveRow> result = new ArrayList<>();
        boolean hasNextPage = true;
        int pageNo = 1;

        while (hasNextPage) {

            Locator rowsLocator = page.locator("#pending_leave table tbody tr");

            int rowCount = rowsLocator.count();

            //System.out.println("-----------------------------------------------------------");
            //System.out.println("Pending Leaves - Page " + pageNo + " | Rows: " + rowCount);

            for (int i = 0; i < rowCount; i++) {

                Locator row = rowsLocator.nth(i);
                List<Locator> cells = row.locator("td").all();

                // must have Leave Approver column
                if (cells.size() < 7) continue;

                String approver = cells.get(6).innerText().trim();
                if (approver.isEmpty() || approver.equals("-")) continue;

                String employee  = cells.get(1).innerText().trim();
                String startDate = cells.get(2).innerText().trim();
                String endDate   = cells.get(3).innerText().trim();
                String daysText  = cells.get(4).innerText().trim();
                String type      = cells.get(8).innerText().trim();
                String reason    = cells.get(5).innerText().trim();

                try {
                    double days = Double.parseDouble(daysText);
                    result.add(new PendingLeaveRow(
                            employee, startDate, endDate, days, type, reason
                    ));
                } catch (Exception ignored) {}
            }

            // ---------------- NEXT BUTTON LOGIC ----------------
            Locator nextButton = page.getByRole(
                    AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Next")
            );

            if (nextButton.count() == 0 || !nextButton.isEnabled()) {
                hasNextPage = false;
                //System.out.println("No more pending pages.");
                break;
            }

            // Scroll first (important)
            nextButton.scrollIntoViewIfNeeded();

            // Small stabilization wait
            page.waitForTimeout(500);

            nextButton.click();
            System.out.println("Clicked Next page");

            // Wait for new page data to load
            page.waitForLoadState(LoadState.NETWORKIDLE);

            pageNo++;
        }

        return result;
    }

    public void openPendingLeaves() {
        page.locator("#pending_link").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

}
