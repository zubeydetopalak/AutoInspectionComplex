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

public class StationCapacityTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- TEST VERİLERİ ---
    private final String targetPlate = "99ZZZ123";
    private final String targetStation = "ST-GEN-01";

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void testCapacityRestoresAfterCompletion() {
        driver.get("http://localhost:3000");

        // 1. GİRİŞ
        System.out.println("--- Adım 1: Giriş Yapılıyor ---");
        performLogin("secretary", "password");

        // 2. İLK KAPASİTE ÖLÇÜMÜ
        System.out.println("--- Adım 2: İlk ölçüm yapılıyor ---");
        safeSwitchTab("Station Status", "Station Overview");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("stations-grid")));
        String stationCardXpath = "//div[contains(@class, 'station-card')][.//h3[text()='" + targetStation + "']]";
        WebElement stationCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(stationCardXpath)));
        highlightElement(stationCard, "yellow");

        WebElement capacityTextElement = stationCard.findElement(By.xpath(".//p[contains(., 'Capacity:')]"));
        int initialCapacity = parseCapacity(capacityTextElement.getText());
        System.out.println("✅ Başlangıç Kapasitesi: " + initialCapacity);
        
        sleep(1000);

        // 3. RANDEVU SAYFASINA GEÇ
        System.out.println("--- Adım 3: Randevu sayfasına geçiliyor ---");
        safeSwitchTab("Customers & Appointments", "Customer Management");

        System.out.println("'Show All Appointments' butonuna basılıyor...");
        WebElement showAllBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Show All Appointments')]")
        ));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", showAllBtn);

        System.out.println("Araç (" + targetPlate + ") bulunuyor...");
        WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//tr[td[contains(text(), '" + targetPlate + "')]]")
        ));
        
        WebElement editBtn = row.findElement(By.xpath(".//button[contains(text(), 'Edit Status')]"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", editBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));
        
        // --- KRİTİK DÜZELTME BAŞLANGICI ---
        // 4. GÜNCELLEME İŞLEMİ VE DOĞRULAMA
        System.out.println("--- Adım 4: Statü COMPLETED yapılıyor ---");
        
        Select statusSelect = new Select(driver.findElement(By.tagName("select")));
        statusSelect.selectByValue("COMPLETED");
        
        // Seçimin React tarafından algılandığından emin olmak için ufak bir bekleme
        sleep(500);

        System.out.println("Update butonuna basılıyor...");
        WebElement updateBtn = driver.findElement(By.xpath("//button[contains(text(), 'Update')]"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", updateBtn);
        
        // DÜZELTME: React'ın "Appointment status updated!" mesajını görene kadar bekle.
        // Bu mesaj çıkmadan modalı kapatırsan işlem veritabanına gitmeyebilir.
        try {
            System.out.println("Başarı mesajı bekleniyor...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Appointment status updated')]")
            ));
            System.out.println("✅ Başarı mesajı görüldü! Güncelleme tamam.");
        } catch (TimeoutException e) {
            System.out.println("⚠️ UYARI: Başarı mesajı yakalanamadı, ama devam ediliyor.");
        }

        // Mesajı gördükten sonra modalı kapat
        WebElement closeBtn = driver.findElement(By.xpath("//button[contains(text(), 'Close')]"));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", closeBtn);
        
        // Modalın tamamen kaybolmasını bekle
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
        // --- KRİTİK DÜZELTME BİTİŞİ ---

        // 5. SONUCU KONTROL ET
        System.out.println("--- Adım 5: Sonucu kontrol etmek için dönülüyor ---");
        safeSwitchTab("Station Status", "Station Overview");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("stations-grid")));
        sleep(1000); 

        // Kartı tekrar bul (DOM yenilendi)
        WebElement finalCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(stationCardXpath)));
        WebElement finalCapText = finalCard.findElement(By.xpath(".//p[contains(., 'Capacity:')]"));
        
        int finalCapacity = parseCapacity(finalCapText.getText());
        System.out.println("✅ Son Kapasite: " + finalCapacity);

        highlightElement(finalCard, "#90EE90"); 

        // 6. DOĞRULAMA
        Assertions.assertEquals(initialCapacity + 1, finalCapacity, 
            "HATA: Kapasite artmadı! (Eski: " + initialCapacity + ", Yeni: " + finalCapacity + ")");
            
        System.out.println("🚀 TEST BAŞARILI: Kapasite beklendiği gibi arttı.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- YARDIMCI METOTLAR ---

    private void safeSwitchTab(String tabName, String expectedHeader) {
        try {
            String xpath = "//li[contains(text(), '" + tabName + "')]";
            WebElement tab = driver.findElement(By.xpath(xpath));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h2"), expectedHeader));
        } catch (Exception e) {
            System.out.println("Menü geçişi tekrar deneniyor: " + tabName);
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(text(), '" + tabName + "')]")));
            tab.click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h2"), expectedHeader));
        }
    }

    private int parseCapacity(String text) {
        try {
            String numberPart = text.split(":")[1].trim();
            return Integer.parseInt(numberPart);
        } catch (Exception e) {
            System.out.println("Sayı okunamadı: " + text);
            throw e;
        }
    }

    private void performLogin(String user, String pass) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='text']")));
        usernameField.clear();
        usernameField.sendKeys(user);
        driver.findElement(By.xpath("//input[@type='password']")).sendKeys(pass);
        driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dashboard-container")));
    }

    private void highlightElement(WebElement element, String color) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.backgroundColor='" + color + "'", element);
    }
    
    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }
}