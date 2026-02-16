package locators;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class LeaveApplicationsLocators {

    public final Locator leaveApplicationsLink;
    public final Locator projectDropdown;
    public final Locator fromDateInput;
    public final Locator toDateInput;
    public final Locator searchButton;

    public final Locator leaveHistoryTab;
    public final Locator leaveHistoryRows;
    public final Locator leaveHistoryNext;

    public final Locator pendingTab;
    public final Locator pendingRows;
    public final Locator pendingNext;

    public LeaveApplicationsLocators(Page page) {

        leaveApplicationsLink = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(" Leave Applications")
        );

        projectDropdown = page.locator("#project_id");

        fromDateInput = page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("From Date")
        );

        toDateInput = page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("To Date")
        );

        searchButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search")
        );

        leaveHistoryTab = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Leave History")
        );

        leaveHistoryRows = page.locator("table tbody tr");

        leaveHistoryNext = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Next")
        );

        pendingTab = page.locator("#pending_link");

        pendingRows = page.locator("#pending_leave table tbody tr");

        pendingNext = page.locator("#pending_leave")
                .getByRole(
                        AriaRole.LINK,
                        new Locator.GetByRoleOptions().setName("Next")
                );
    }
}
