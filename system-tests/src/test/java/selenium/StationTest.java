package selenium;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class StationTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();

        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", true);

        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void testCreateStationAndVerify() throws InterruptedException {
        driver.get("http://localhost:3000");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='username' or contains(@placeholder, 'User') or contains(@placeholder, 'Kullanıcı') or @type='text']")
        ));
        usernameField.clear();
        usernameField.sendKeys("secretary");

        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @name='password']"));
        passwordField.clear();
        passwordField.sendKeys("password");

        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit' or contains(text(), 'Login') or contains(text(), 'Giriş')]"));
        clickSafely(loginBtn);

        WebElement stationMenuLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(), 'Station Status')]")
        ));
        clickSafely(stationMenuLink);

        WebElement addNewStationBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Add New Station')]")
        ));
        clickSafely(addNewStationBtn);

        String uniqueStationCode = "ST-" + System.currentTimeMillis();
        System.out.println("Eklenen İstasyon Kodu: " + uniqueStationCode);

        WebElement stationCodeInput = driver.findElement(By.xpath("//label[contains(text(),'Station Code')]/following-sibling::input"));
        stationCodeInput.sendKeys(uniqueStationCode);

        WebElement capacityInput = driver.findElement(By.xpath("//label[contains(text(),'Capacity')]/following-sibling::input"));
        capacityInput.clear();
        capacityInput.sendKeys("20");

        WebElement createButton = driver.findElement(By.xpath("//button[contains(text(), 'Create Station')]"));
        clickSafely(createButton);

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Station created successfully!')]")
        ));
        Assertions.assertTrue(successMessage.isDisplayed(), "Başarı mesajı çıkmadı!");


        try {
            WebElement newStationCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h3[contains(text(), '" + uniqueStationCode + "')]")
            ));
            
            System.out.println("✅ İstasyon kartı bulundu: " + uniqueStationCode);
            Assertions.assertTrue(newStationCard.isDisplayed());

        } catch (TimeoutException e) {
            System.out.println("⚠️ Listede hemen görünmedi, sayfa yenileniyor (F5)...");
            
            driver.navigate().refresh();

            WebElement stationMenuLinkRetry = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[contains(text(), 'Station Status')]")
            ));
            clickSafely(stationMenuLinkRetry);

            WebElement newStationCardAfterRefresh = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h3[contains(text(), '" + uniqueStationCode + "')]")
            ));
            
            System.out.println("✅ Sayfa yenilendikten sonra bulundu: " + uniqueStationCode);
            Assertions.assertTrue(newStationCardAfterRefresh.isDisplayed());
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void clickSafely(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (UnhandledAlertException f) {
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
                element.click();
            } catch (Exception ignore) {}
        } catch (ElementClickInterceptedException e) {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }
}