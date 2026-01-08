package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

public class BasePage {

    protected Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    protected void waitForVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(60000));
    }

    protected void clickWhenReady(Locator locator) {
        waitForVisible(locator);
        locator.click();
    }

    protected void fillWhenReady(Locator locator, String value) {
        waitForVisible(locator);
        locator.fill(value);
    }
}
