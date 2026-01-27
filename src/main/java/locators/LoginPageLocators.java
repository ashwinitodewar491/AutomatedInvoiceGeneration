package locators;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class LoginPageLocators {

    public final Locator emailInput;
    public final Locator passwordInput;
    public final Locator signInButton;

    public LoginPageLocators(Page page) {

        emailInput = page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Email *")
        );

        passwordInput = page.getByRole(
                AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Password *")
        );

        signInButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Sign in")
        );
    }
}
