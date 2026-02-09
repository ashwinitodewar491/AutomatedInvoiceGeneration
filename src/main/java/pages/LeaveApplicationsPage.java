package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import locators.LeaveApplicationsLocators;
import models.LeaveRow;
import models.PendingLeaveRow;

import java.util.ArrayList;
import java.util.List;

public class LeaveApplicationsPage {

    private final Page page;
    private final LeaveApplicationsLocators loc;

    public LeaveApplicationsPage(Page page) {
        this.page = page;
        this.loc = new LeaveApplicationsLocators(page);
    }

    // ================= ACTIONS ONLY =================

    public void open() {
        loc.leaveApplicationsLink.click();
        page.waitForURL("**/leave_applications");
    }

    public String applyFilters(String projectId, String from, String to) {

        String projectName = loc.projectDropdown
                .locator("option[value='" + projectId + "']")
                .innerText()
                .trim();

        loc.projectDropdown.selectOption(projectId);
        loc.fromDateInput.fill(from);
        loc.toDateInput.fill(to);
        loc.searchButton.click();

        return projectName;
    }

    public void openLeaveHistory() {
        loc.leaveHistoryTab.click();
        page.waitForFunction(
                "() => document.querySelector('table tbody') !== null"
        );
    }

    public List<LeaveRow> fetchLeaveHistoryRows() {

        List<LeaveRow> result = new ArrayList<>();

        while (true) {

            page.waitForLoadState(LoadState.NETWORKIDLE);

            int count = loc.leaveHistoryRows.count();
            if (count == 0) break;

            String snapshot = loc.leaveHistoryRows.first().innerText();

            for (int i = 0; i < count; i++) {

                Locator row = loc.leaveHistoryRows.nth(i);
                List<Locator> cells = row.locator("td").all();
                if (cells.size() < 9) continue;

                String employee = cells.get(1).innerText().trim();
                String daysText = cells.get(4).innerText().trim();
                String status = cells.get(7).innerText().trim();
                String type = cells.get(8).innerText().trim();

                if (type.equalsIgnoreCase("WFH")) continue;
                if (status.equalsIgnoreCase("Rejected")) continue;

                try {
                    result.add(
                            new LeaveRow(
                                    employee,
                                    Double.parseDouble(daysText)
                            )
                    );
                } catch (Exception ignored) {
                }
            }

            if (!loc.leaveHistoryNext.isEnabled()) break;

            loc.leaveHistoryNext.scrollIntoViewIfNeeded();
            loc.leaveHistoryNext.click();

            try {
                page.waitForFunction(
                        "(text) => document.querySelector('table tbody tr')?.innerText !== text",
                        snapshot
                );
            } catch (Exception e) {
                break;
            }
        }
        return result;
    }

    public void openPendingLeaves() {
        loc.pendingTab.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public List<PendingLeaveRow> fetchPendingLeaves() {

        List<PendingLeaveRow> result = new ArrayList<>();

        while (true) {

            int count = loc.pendingRows.count();
            if (count == 0) break;

            for (int i = 0; i < count; i++) {

                Locator row = loc.pendingRows.nth(i);
                List<Locator> cells = row.locator("td").all();
                if (cells.size() < 9) continue;

                String approver = cells.get(6).innerText().trim();
                if (approver.isEmpty() || approver.equals("-")) continue;

                try {
                    result.add(
                            new PendingLeaveRow(
                                    cells.get(1).innerText().trim(),
                                    cells.get(2).innerText().trim(),
                                    cells.get(3).innerText().trim(),
                                    Double.parseDouble(
                                            cells.get(4).innerText().trim()
                                    ),
                                    cells.get(8).innerText().trim(),
                                    cells.get(5).innerText().trim()
                            )
                    );
                } catch (Exception ignored) {
                }
            }

            if (!loc.pendingNext.isEnabled()) break;

            loc.pendingNext.scrollIntoViewIfNeeded();
            loc.pendingNext.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
        }
        return result;
    }

    public boolean isProjectIdPresent(String projectId) {

        Locator options = page.locator("#project_id option");

        for (int i = 0; i < options.count(); i++) {
            String value = options.nth(i).getAttribute("value");
            if (projectId.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean isProjectNamePresent(String projectName) {

        Locator options = page.locator("#project_id option");

        return options.allInnerTexts()
                .stream()
                .anyMatch(
                        text -> text.trim()
                                .equalsIgnoreCase(projectName)
                );
    }

    public String applyFiltersByProjectName(
            String projectName,
            String fromDate,
            String toDate
    ) {

        Locator projectDropdown = page.locator("#project_id");

        // 1️⃣ Trim input (important for spaces)
        projectName = projectName.trim();

        // 2️⃣ Validate project exists by visible text
        Locator matchingOption = projectDropdown.locator("option")
                .filter(new Locator.FilterOptions().setHasText(projectName));

        if (matchingOption.count() == 0) {
            throw new IllegalStateException(
                    "Project not found in dropdown: " + projectName
            );
        }

        // 3️⃣ Select by VISIBLE TEXT (label)
        projectDropdown.selectOption(
                new SelectOption().setLabel(projectName)
        );

        // 4️⃣ Read back selected project (safety check)
        String selectedProject =
                projectDropdown.locator("option:checked")
                        .innerText()
                        .trim();

        System.out.println("✅ Selected project: " + selectedProject);

        // 5️⃣ Apply date filters
        page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("From Date")
        ).fill(fromDate);

        page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("To Date")
        ).fill(toDate);

        // 6️⃣ Search
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search")
        ).click();

        return selectedProject;
    }
}
