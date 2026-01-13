package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import utils.EnvConfig;

public class LoginPage2 {

    private final Page page;

    public LoginPage2(Page page) {
        this.page = page;
    }

    public void login(String email, String password) {

        page.navigate(EnvConfig.get("BASE_URL"));

        // ✅ WAIT FOR LOGIN FORM
        page.waitForSelector("input[type='email'], input[name='email']");

        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName("Email *"))
                .fill(email);

        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName("Password *"))
                .fill(password);

        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Sign in"))
                .click();

        // ✅ WAIT AFTER LOGIN
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
