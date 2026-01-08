package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

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
        page.waitForSelector("table tbody tr");
    }

    /**
     * @return Map with keys: "PENDING", "HISTORY"
     */
    public Map<String, int[]> calculateLeaveDaysPerEmployee() {

        Map<String, int[]> result = new HashMap<>();
        // int[0] = transaction count
        // int[1] = total leave days

        while (true) {

            // ✅ Wait for table rows
            page.waitForSelector("table tbody tr");

            // ✅ Freeze rows for current page
            List<Locator> rows = page.locator("table tbody tr").all();

            for (Locator row : rows) {

                List<Locator> cells = row.locator("td").all();
                if (cells.size() < 9) continue;

                String employee  = cells.get(1).textContent().trim();
                String leaveType = cells.get(8).textContent().trim();
                String daysText  = cells.get(4).textContent().trim();

                if (leaveType.equalsIgnoreCase("WFH")) continue;

                double days;
                try {
                    days = Double.parseDouble(daysText);
                } catch (Exception e) {
                    continue;
                }

                result.putIfAbsent(employee, new int[]{0, 0});
                result.get(employee)[0]++;
                result.get(employee)[1] += days;
            }

            // 🔑 STEP 1: check if Next button EXISTS
            Locator nextLi = page.locator(
                    "#DataTables_Table_0_wrapper li.paginate_button.next"
            );

            if (nextLi.count() == 0) {
                // No pagination at all
                break;
            }

            // 🔑 STEP 2: check if it is disabled
            String nextClass = nextLi.first().getAttribute("class");
            if (nextClass != null && nextClass.contains("disabled")) {
                break;
            }

            // 🔑 STEP 3: click Next
            nextLi.locator("button").click();

            // ✅ wait for DataTables redraw
            page.waitForTimeout(800);
        }

        return result;
    }
}