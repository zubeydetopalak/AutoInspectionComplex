package selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InspectorPassWithMinorsTest2 {

    private WebDriver driver;
    private WebDriverWait wait;

    private final String targetPlate = "01ABC123";

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void testInspectionThreeMinorDefects() {
        driver.get("http://localhost:3000");

        System.out.println("--- Adım 1: Inspector Girişi ---");
        performLogin("zub", "zub");

        System.out.println("--- Adım 2: Plaka Aranıyor: " + targetPlate + " ---");
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Search by Plate Code']")
        ));
        searchInput.clear();
        searchInput.sendKeys(targetPlate);
        
        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(searchBtn);
        sleep(1000);

        System.out.println("--- Adım 3: İnceleme Ekranı Açılıyor ---");
        WebElement startBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'Inspection')]")
        ));
        clickSafely(startBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));

        System.out.println("--- Adım 4: Far Ayarları (Sol) Ekleniyor ---");
        addInspectionDetail("Far Ayarları", "FAIL", "Sol far ayarı bozuk");

        System.out.println("--- Adım 5: Silecekler Ekleniyor ---");
        addInspectionDetail("Silecekler", "FAIL", "Silecek lastiği yıpranmış");

        System.out.println("--- Adım 6: Far Ayarları (Sağ) Ekleniyor ---");
        addInspectionDetail("Far Ayarları", "FAIL", "Sağ far biraz yukarı bakıyor");

        System.out.println("--- Adım 7: İnceleme Tamamlanıyor ---");

        try {
            WebElement completeBtn = driver.findElement(By.xpath("//div[contains(@class, 'modal-footer')]//button[contains(text(), 'Complete Inspection')]"));
            clickSafely(completeBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Inspection completed!')]")));

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
            sleep(1000); 
        } catch (Exception e) {
            System.out.println("Complete butonu bulunamadı veya işlem zaten yapılmış. Kapatılıyor.");
            try {
                driver.findElement(By.className("close-btn")).click();
                sleep(500);
            } catch (Exception ex) {}
        }

        System.out.println("--- Adım 8: Sonuç Kontrolü ---");

        WebElement showBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'Show Inspection')]")
        ));
        clickSafely(showBtn);

        WebElement resultElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//p[strong[contains(text(), 'Result:')]]")
        ));
        
        String resultText = resultElement.getText().toUpperCase();
        System.out.println("Ekranda Görünen Sonuç: " + resultText);

        ((JavascriptExecutor) driver).executeScript("arguments[0].style.backgroundColor='orange'", resultElement);

        boolean isPassOrConditional = resultText.contains("ŞARTLI") || 
                                      resultText.contains("CONDITIONAL") || 
                                      resultText.contains("PASS") || 
                                      resultText.contains("GEÇTİ");
        
        if (isPassOrConditional) {
            System.out.println("✅ TEST BAŞARILI: Araç 3 hafif kusurla GEÇTİ/ŞARTLI GEÇTİ.");
        } else {
            System.out.println("❌ TEST HATALI: Sonuç beklenmedik: " + resultText);
        }
        
        Assertions.assertTrue(isPassOrConditional, 
            "Sonuç GEÇTİ veya ŞARTLI olmalı! Görünen: " + resultText);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void addInspectionDetail(String keyword, String status, String note) {

        WebElement checklistSelectElem = driver.findElement(By.xpath("//label[contains(text(), 'Checklist Item')]/..//select"));
        Select selectItem = new Select(checklistSelectElem);
        
        boolean found = false;
        List<WebElement> options = selectItem.getOptions();
        
        for (WebElement option : options) {
            if (option.getText().contains(keyword)) {
                selectItem.selectByVisibleText(option.getText());
                found = true;
                break;
            }
        }
        
        if (!found) {
            System.out.println("UYARI: Dropdown'da '" + keyword + "' içeren seçenek bulunamadı. İlk seçenek seçiliyor.");
            if(options.size() > 1) selectItem.selectByIndex(1);
        }

        WebElement statusSelectElem = driver.findElement(By.xpath("//label[contains(text(), 'Status')]/..//select"));
        Select selectStatus = new Select(statusSelectElem);
        selectStatus.selectByVisibleText(status); 

        WebElement noteInput = driver.findElement(By.xpath("//input[@placeholder='Inspector Note']"));
        noteInput.clear();
        noteInput.sendKeys(note);

        WebElement addBtn = driver.findElement(By.xpath("//button[text()='Add']"));
        clickSafely(addBtn);
        
        sleep(500);
    }

    private void performLogin(String user, String pass) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text']")
        ));
        usernameField.clear();
        usernameField.sendKeys(user);
        driver.findElement(By.xpath("//input[@type='password']")).sendKeys(pass);
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'Sign In') or contains(text(), 'Login')]"));
        clickSafely(loginBtn);
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("dashboard"),
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Auto Service')]"))
        ));
    }

    private void clickSafely(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
    
    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }
}