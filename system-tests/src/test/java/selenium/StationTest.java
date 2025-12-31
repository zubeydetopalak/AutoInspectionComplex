package com.zubeyde.auto.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StationTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // --- 1. AYARLAR: GOOGLE ALERT VE ŞİFRE UYARILARINI KAPATMA ---
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        
        // Şifre yöneticisi ve "şifreniz sızdırıldı" uyarılarını kapat
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", true);

        options.setExperimentalOption("prefs", prefs);
        
        // Bildirimleri engelle
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

        // --- GİRİŞ YAP ---
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='username' or contains(@placeholder, 'User') or contains(@placeholder, 'Kullanıcı') or @type='text']")
        ));
        usernameField.clear();
        usernameField.sendKeys("secretary");

        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @name='password']"));
        passwordField.clear();
        passwordField.sendKeys("password");

        // Giriş Butonu
        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit' or contains(text(), 'Login') or contains(text(), 'Giriş')]"));
        clickSafely(loginBtn);

        // --- İSTASYON SEKMESİNE GEÇ ---
        // React kodunda: onClick={() => setActiveTab('stations')}
        WebElement stationMenuLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(), 'Station Status')]")
        ));
        clickSafely(stationMenuLink);

        // --- "ADD NEW STATION" BUTONUNA TIKLA ---
        WebElement addNewStationBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Add New Station')]")
        ));
        clickSafely(addNewStationBtn);

        // --- FORMU DOLDUR ---
        String uniqueStationCode = "ST-" + System.currentTimeMillis();
        System.out.println("Eklenen İstasyon Kodu: " + uniqueStationCode);

        // İstasyon Kodu (Station Code)
        WebElement stationCodeInput = driver.findElement(By.xpath("//label[contains(text(),'Station Code')]/following-sibling::input"));
        stationCodeInput.sendKeys(uniqueStationCode);

        // Kapasite (Capacity)
        WebElement capacityInput = driver.findElement(By.xpath("//label[contains(text(),'Capacity')]/following-sibling::input"));
        capacityInput.clear();
        capacityInput.sendKeys("20");

        // KAYDET BUTONU
        // React kodunda: <button type="submit" ...>Create Station</button>
        WebElement createButton = driver.findElement(By.xpath("//button[contains(text(), 'Create Station')]"));
        clickSafely(createButton);

        // --- DOĞRULAMA 1: BAŞARI MESAJI ---
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Station created successfully!')]")
        ));
        Assertions.assertTrue(successMessage.isDisplayed(), "Başarı mesajı çıkmadı!");

        // --- DOĞRULAMA 2: LİSTEDE GÖRÜNME ---
        // React kodunda: <h3>{station.stationCode}</h3> şeklinde render ediliyor.
        // Grid yapısı olduğu için tablo aramıyoruz, direkt başlık (h3) arıyoruz.

        try {
            // uniqueStationCode içeren herhangi bir elementin görünmesini bekle
            WebElement newStationCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h3[contains(text(), '" + uniqueStationCode + "')]")
            ));
            
            System.out.println("✅ İstasyon kartı bulundu: " + uniqueStationCode);
            Assertions.assertTrue(newStationCard.isDisplayed());

        } catch (TimeoutException e) {
            // Eğer hemen görünmezse (React state gecikmesi), sayfayı yenile ve tekrar bak.
            System.out.println("⚠️ Listede hemen görünmedi, sayfa yenileniyor (F5)...");
            
            driver.navigate().refresh();
            
            // Yenileyince varsayılan olarak "Customers" sekmesi açılıyor olabilir.
            // Tekrar "Station Status" sekmesine tıklamamız lazım.
            WebElement stationMenuLinkRetry = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[contains(text(), 'Station Status')]")
            ));
            clickSafely(stationMenuLinkRetry);
            
            // Tekrar ara
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

    // --- GÜVENLİ TIKLAMA YÖNTEMİ ---
    // React render olurken veya Alert çıkarsa tıklamayı tekrar dener.
    private void clickSafely(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (UnhandledAlertException f) {
            // Tıklarken Alert çıkarsa kapat ve tekrar dene
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
                element.click();
            } catch (Exception ignore) {}
        } catch (ElementClickInterceptedException e) {
            // Başka bir element üstüne geldiyse (örn: message overlay), JS ile tıkla
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }
}