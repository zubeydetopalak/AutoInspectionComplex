package selenium;


import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class InspectorLoginTest {

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testInspectorLoginFlow() {
        driver.get("http://localhost:3000");

        WebElement usernameInput = driver.findElement(By.cssSelector("input[type='text']"));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));

        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]"));

        usernameInput.clear();
        usernameInput.sendKeys("zub");

        passwordInput.clear();
        passwordInput.sendKeys("zub");

        loginBtn.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

       wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(), 'Inspection Management')]")
        ));

        String currentUrl = driver.getCurrentUrl();
        System.out.println("Giriş sonrası URL: " + currentUrl);

        assertTrue(currentUrl.contains("inspector-dashboard"), "Login başarısız! Dashboard'a gidilemedi.");
    }
}