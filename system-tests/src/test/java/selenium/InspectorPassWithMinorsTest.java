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
import java.util.List;
import java.util.Map;

public class InspectorPassWithMinorsTest {

    private WebDriver driver;
    private WebDriverWait wait;
    
    // Hedef Plaka (Veritabanında randevusu olan bir plaka olmalı)
    private final String targetPlate = "04ABC123";

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
    public void testInspectionMinorDefectsPass() {
        driver.get("http://localhost:3000");
        
        // 1. GİRİŞ YAP (Inspector)
        System.out.println("--- Adım 1: Inspector Girişi Yapılıyor ---");
        performLogin("zub", "zub");

        // 2. PLAKA ARA
        System.out.println("--- Adım 2: Plaka Aranıyor: " + targetPlate + " ---");
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Search by Plate Code']")
        ));
        searchInput.clear();
        searchInput.sendKeys(targetPlate);
        
        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(searchBtn);
        sleep(1000);

        // 3. START/SHOW INSPECTION'A BAS
        System.out.println("--- Adım 3: İnceleme Ekranı Açılıyor ---");
        WebElement startBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'Start Inspection') or contains(text(), 'Show Inspection')]")
        ));
        clickSafely(startBtn);

        // Modalın açılmasını bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));

        // 4. FAR AYARLARI -> FAIL EKLE (Hafif Kusur Senaryosu)
        System.out.println("--- Adım 4: Far Ayarları (FAIL) Ekleniyor ---");
        addInspectionDetail("Far Ayarları", "FAIL", "Farlar yukarı bakıyor");

        // 5. SİLECEKLER -> FAIL EKLE (Hafif Kusur Senaryosu)
        System.out.println("--- Adım 5: Silecekler (FAIL) Ekleniyor ---");
        addInspectionDetail("Silecekler", "FAIL", "Arka silecek lastiği yok");

        // 6. INSPECTION'I TAMAMLA
        System.out.println("--- Adım 6: İnceleme Tamamlanıyor (Complete Inspection) ---");
        
        // Eğer önceden tamamlanmışsa buton çıkmayabilir, kontrol ediyoruz
        try {
            WebElement completeBtn = driver.findElement(By.xpath("//button[contains(text(), 'Complete Inspection')]"));
            clickSafely(completeBtn);
            // Modalın kapanmasını bekle
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
            sleep(1000); 
        } catch (NoSuchElementException e) {
            System.out.println("Complete butonu bulunamadı, muayene zaten tamamlanmış olabilir. Kontrole geçiliyor.");
             // Modal açıksa kapatalım ki tekrar açıp sonucu görelim
            try {
                driver.findElement(By.className("close-btn")).click();
                sleep(500);
            } catch (Exception ex) {}
        }

        // 7. SONUCU KONTROL ET (SHOW DETAILS)
        System.out.println("--- Adım 7: Sonuç 'GEÇTİ' mi diye kontrol ediliyor ---");
        
        // Butona tekrar tıkla (Show Inspection)
        WebElement showBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'Show Inspection')]")
        ));
        clickSafely(showBtn);
        
        // Sonuç Yazısını Bul
        WebElement resultElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//p[strong[contains(text(), 'Result:')]]")
        ));
        
        String resultText = resultElement.getText().toUpperCase();
        System.out.println("Ekranda Görünen Sonuç: " + resultText);

        // Görsellik: Sonucu yeşil yap
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.backgroundColor='#90EE90'", resultElement);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", resultElement);

        // DOĞRULAMA: GEÇTİ veya PASS kelimesini bekle
        boolean isPass = resultText.contains("PASS") || resultText.contains("GEÇTİ") || resultText.contains("SUCCESS");
        
        if (isPass) {
            System.out.println("✅ TEST BAŞARILI: Araç hafif kusurlarla GEÇTİ.");
        } else {
            System.out.println("❌ TEST HATALI: Sonuç GEÇTİ olmalıydı ama ekranda şu yazıyor: " + resultText);
        }
        
        Assertions.assertTrue(isPass, "Test sonucu GEÇTİ veya PASS olmalı! Görünen: " + resultText);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- YARDIMCI METOTLAR ---

    private void addInspectionDetail(String checklistItemText, String status, String note) {
        // Dropdown'da ismi ara ve seç (Tam eşleşme veya içerir mantığı)
        WebElement checklistDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Checklist Item')]/following-sibling::select"));
        Select selectItem = new Select(checklistDropdown);
        
        boolean itemFound = false;
        List<WebElement> options = selectItem.getOptions();
        for (WebElement option : options) {
            // Büyük küçük harf duyarlılığını kaldırmak için toLowerCase kullanabiliriz
            if (option.getText().toLowerCase().contains(checklistItemText.toLowerCase())) {
                selectItem.selectByVisibleText(option.getText());
                itemFound = true;
                break;
            }
        }
        
        if (!itemFound) {
            Assertions.fail("Dropdown içinde şu seçenek bulunamadı: " + checklistItemText);
        }

        // Status Seç (PASS/FAIL)
        WebElement statusDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Status')]/following-sibling::select"));
        Select selectStatus = new Select(statusDropdown);
        if (status.equalsIgnoreCase("PASS")) {
            selectStatus.selectByVisibleText("PASS"); 
        } else {
            selectStatus.selectByVisibleText("FAIL");
        }

        // Not Ekle
        WebElement noteInput = driver.findElement(By.xpath("//input[@placeholder='Inspector Note']"));
        noteInput.clear();
        noteInput.sendKeys(note);

        // Add Butonuna Bas
        WebElement addBtn = driver.findElement(By.xpath("//button[contains(text(), 'Add') and not(contains(text(), 'Vehicle'))]"));
        clickSafely(addBtn);
        
        // Tabloya eklendiğini görmek için kısa bekleme
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