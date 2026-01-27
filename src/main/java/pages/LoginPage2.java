package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import locators.LoginPageLocators;
import utils.EnvConfig;

public class LoginPage2 {

    private final Page page;
    private final LoginPageLocators loc;

    public LoginPage2(Page page) {
        this.page = page;
        this.loc = new LoginPageLocators(page);
    }

    public void login(String email, String password) {

        page.navigate(EnvConfig.get("BASE_URL"));

        // wait for login form
        loc.emailInput.waitFor();

        loc.emailInput.fill(email);
        loc.passwordInput.fill(password);
        loc.signInButton.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
