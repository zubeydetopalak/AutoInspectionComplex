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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class StationUpdateTest {

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

        options.addArguments("--disable-notifications"); // Bildirimleri engelle
        options.addArguments("--disable-popup-blocking"); // Pop-upları engelle
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        options.addArguments("--disable-features=PasswordLeakDetection");

        driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void testUpdateStationToClosed() {
        driver.get("http://localhost:3000");

        handlePotentialAlert();

        System.out.println("Login işlemi başlıyor...");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text' and contains(@class, 'form-input')]")
        ));
        usernameField.clear();
        usernameField.sendKeys("secretary");

        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' and contains(@class, 'form-input')]"));
        passwordField.clear();
        passwordField.sendKeys("password");

        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]"));
        clickSafely(loginBtn);

        wait.until(ExpectedConditions.urlContains("dashboard"));
        handlePotentialAlert();

        System.out.println("Menüye gidiliyor...");
        
        WebElement stationLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(), 'Station Status')]")
        ));
        clickSafely(stationLink);

        System.out.println("Açık (Open) olan bir istasyon aranıyor...");

        WebElement targetCard;
        try {
            targetCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//div[contains(@class, 'station-card')][.//p[contains(., 'Status: Open')]])[1]")
            ));
        } catch (TimeoutException e) {
            Assertions.fail("HATA: 'Status: Open' olan istasyon bulunamadı.");
            return;
        }

        String stationCode = targetCard.findElement(By.tagName("h3")).getText();
        System.out.println("İşlem yapılacak istasyon: " + stationCode);

        WebElement editBtn = targetCard.findElement(By.xpath(".//button[contains(text(), 'Edit')]"));
        clickSafely(editBtn);

        System.out.println("Statü değiştiriliyor...");
        
        WebElement statusDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
             By.xpath("//div[contains(@class, 'station-card')]//select")
        ));

        Select select = new Select(statusDropdown);
        select.selectByVisibleText("Closed");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(text(), 'Save')]"));
        clickSafely(saveBtn);

        handlePotentialAlert();

        System.out.println("Doğrulama yapılıyor...");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Station updated successfully')]")
        ));

        try {
            WebElement updatedCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'station-card')]" +
                         "[.//h3[text()='" + stationCode + "']]" +
                         "[.//p[contains(., 'Status: Closed')]]")
            ));
            
            Assertions.assertTrue(updatedCard.isDisplayed());
            System.out.println("✅ TEST BAŞARILI! " + stationCode + " kapatıldı.");
            
        } catch (TimeoutException e) {
            System.out.println("⚠️ Arayüz yenilenmedi, sayfa refresh ediliyor...");
            driver.navigate().refresh();
            handlePotentialAlert();
            
            WebElement menuRetry = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(), 'Station Status')]")
            ));
            clickSafely(menuRetry);
            
            WebElement verifiedElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'station-card')][.//h3[text()='" + stationCode + "']][.//p[contains(., 'Status: Closed')]]")
            ));
            Assertions.assertTrue(verifiedElement.isDisplayed());
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void handlePotentialAlert() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(500));
            shortWait.until(ExpectedConditions.alertIsPresent());
            
            Alert alert = driver.switchTo().alert();
            System.out.println("⚠️ Beklenmedik bir uyarı yakalandı: " + alert.getText());
            alert.accept();

            Thread.sleep(200); 
        } catch (TimeoutException | NoAlertPresentException e) {
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void clickSafely(WebElement element) {
        handlePotentialAlert();
        
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            System.out.println("Normal tıklama başarısız, JS ile ve Alert kontrolüyle deneniyor...");

            handlePotentialAlert();
            
            try {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                executor.executeScript("arguments[0].click();", element);
            } catch (Exception ex) {
                System.out.println("Kritik Hata: Tıklama gerçekleştirilemedi -> " + ex.getMessage());
                Assertions.fail("Elemente tıklanamadı: " + element.toString());
            }
        }
    }
}