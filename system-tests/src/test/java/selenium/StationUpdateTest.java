package com.zubeyde.auto.selenium;

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
import java.util.Map;

public class StationUpdateTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // --- 1. AYARLAR (Chrome Pop-up Engelleyici) ---
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        
        // 1. Şifre kaydetme balonunu kapat
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        
        // 2. "Şifreniz ifşa oldu" / "Şifre sızıntısı" uyarılarını kapat (Kritik nokta burası)
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", true); 

        options.setExperimentalOption("prefs", prefs);
        
        // 3. Tarayıcı özelliklerini devre dışı bırak
        options.addArguments("--disable-notifications"); // Bildirimleri engelle
        options.addArguments("--disable-popup-blocking"); // Pop-upları engelle
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        // Şifre sızıntı tespitini feature seviyesinde de kapat
        options.addArguments("--disable-features=PasswordLeakDetection");

        driver = new ChromeDriver(options);
        
        // Implicit Wait: Element ararken standart bekleme
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        
        // Explicit Wait: Özel durumlar için bekleme
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void testUpdateStationToClosed() {
        driver.get("http://localhost:3000");

        // Olası başlangıç uyarılarını temizle
        handlePotentialAlert();

        // --- 2. GİRİŞ YAP (LOGIN) ---
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

        // --- DASHBOARD KONTROLÜ ---
        wait.until(ExpectedConditions.urlContains("dashboard"));
        handlePotentialAlert(); // Login sonrası uyarı çıkarsa kapat

        // --- 3. STATION MENÜSÜNE GİT ---
        System.out.println("Menüye gidiliyor...");
        
        WebElement stationLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(), 'Station Status')]")
        ));
        clickSafely(stationLink);

        // --- 4. HEDEF KARTI BUL ---
        System.out.println("Açık (Open) olan bir istasyon aranıyor...");

        WebElement targetCard;
        try {
            // Ara ara DOM değişebileceği için stale element hatasına karşı tekrar denemeli yapı
            targetCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//div[contains(@class, 'station-card')][.//p[contains(., 'Status: Open')]])[1]")
            ));
        } catch (TimeoutException e) {
            Assertions.fail("HATA: 'Status: Open' olan istasyon bulunamadı.");
            return;
        }

        String stationCode = targetCard.findElement(By.tagName("h3")).getText();
        System.out.println("İşlem yapılacak istasyon: " + stationCode);

        // --- 5. EDIT BUTONUNA BAS ---
        WebElement editBtn = targetCard.findElement(By.xpath(".//button[contains(text(), 'Edit')]"));
        clickSafely(editBtn);

        // --- 6. STATÜYÜ "CLOSED" YAP ---
        System.out.println("Statü değiştiriliyor...");
        
        WebElement statusDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
             By.xpath("//div[contains(@class, 'station-card')]//select")
        ));

        Select select = new Select(statusDropdown);
        select.selectByVisibleText("Closed");

        // --- 7. SAVE (KAYDET) ---
        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(text(), 'Save')]"));
        clickSafely(saveBtn);
        
        // Kayıt sonrası alert çıkabilir, kontrol et
        handlePotentialAlert();

        // --- 8. KONTROL ET (VERIFICATION) ---
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
            handlePotentialAlert(); // Refresh sonrası alert kontrolü
            
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

    // --- YARDIMCI METOT: ALARM / POPUP KAPATICI ---
    private void handlePotentialAlert() {
        try {
            // Çok kısa bir süre alert var mı diye bak (0.5 saniye)
            // Uzun beklersek testi yavaşlatır, o yüzden kısa tutuyoruz.
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(500));
            shortWait.until(ExpectedConditions.alertIsPresent());
            
            Alert alert = driver.switchTo().alert();
            System.out.println("⚠️ Beklenmedik bir uyarı yakalandı: " + alert.getText());
            alert.accept(); // OK tuşuna bas
            
            // Alert kapandıktan sonra sayfanın kendine gelmesi için minik bir bekleme
            Thread.sleep(200); 
        } catch (TimeoutException | NoAlertPresentException e) {
            // Alert yoksa sorun yok, devam et.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- GÜVENLİ TIKLAMA ---
    private void clickSafely(WebElement element) {
        // Tıklamadan önce önünde engelleyen bir alert var mı?
        handlePotentialAlert();
        
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            // Eğer normal tıklama başarısızsa (örn: başka element üstteyse)
            System.out.println("Normal tıklama başarısız, JS ile ve Alert kontrolüyle deneniyor...");
            
            // Tekrar alert kontrolü yap (bazen hover yapınca alert çıkar)
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