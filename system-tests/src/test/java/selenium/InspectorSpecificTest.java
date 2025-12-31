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

public class InspectorSpecificTest {

    private WebDriver driver;
    private WebDriverWait wait;
    
    // Hedef Plaka
    private final String targetPlate = "34ABC123";

    @BeforeEach
    public void setUp() {
        // --- Standart Tarayıcı Ayarları ---
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
    public void testInspectionFailScenario() {
        driver.get("http://localhost:3000");
        
        // 1. GİRİŞ YAP (Inspector: zub/zub)
        System.out.println("--- Adım 1: Inspector Girişi Yapılıyor ---");
        performLogin("zub", "zub");

        // 2. PLAKA ARA (34ABC123)
        System.out.println("--- Adım 2: Plaka Aranıyor: " + targetPlate + " ---");
        
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Search by Plate Code']")
        ));
        searchInput.clear();
        searchInput.sendKeys(targetPlate);
        
        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(searchBtn);
        
        sleep(1000); // Sonuçların filtrelenmesi için bekle

        // 3. START INSPECTION'A BAS
        System.out.println("--- Adım 3: İnceleme Başlatılıyor ---");
        
        // Tabloda bu plakanın olduğu satırı ve butonu bul
        WebElement startBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'Start Inspection') or contains(text(), 'Show Inspection')]")
        ));
        
        // Eğer zaten Show Inspection ise (önceden yapılmışsa) testin mantığı değişebilir ama biz tıkla diyoruz.
        clickSafely(startBtn);

        // Modalın açılmasını bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));

        // 4. LASTİK DİŞ DERİNLİĞİ -> PASS EKLE
        System.out.println("--- Adım 4: Lastik Diş Derinliği (PASS) Ekleniyor ---");
        addInspectionDetail("Lastik Diş Derinliği", "PASS", "Lastikler iyi durumda");

        // 5. FREN SİSTEMİ KONTROLÜ -> FAIL EKLE
        System.out.println("--- Adım 5: Fren Sistemi Kontrolü (FAIL) Ekleniyor ---");
        addInspectionDetail("Fren Sistemi Kontrolü", "FAIL", "Fren balataları bitik");

        // 6. COMPLETE INSPECTION'A BAS
        System.out.println("--- Adım 6: İnceleme Tamamlanıyor ---");
        
        WebElement completeBtn = driver.findElement(By.xpath("//button[contains(text(), 'Complete Inspection')]"));
        clickSafely(completeBtn);
        
        // Modalın kapanmasını bekle (Veya başarı mesajını)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
        sleep(1000); // Veritabanı güncellemesi için kısa bekleme

        // 7. SHOW DETAILS (SHOW INSPECTION) BAS VE KONTROL ET
        System.out.println("--- Adım 7: Sonuç Kontrol Ediliyor ---");
        
        // Buton artık "Show Inspection" olmalı, tekrar tıkla
        WebElement showBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'Show Inspection')]")
        ));
        clickSafely(showBtn);
        
        // Modal tekrar açılınca sonucu kontrol et
        WebElement resultElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//p[strong[contains(text(), 'Result:')]]")
        ));
        
        String resultText = resultElement.getText().toUpperCase();
        System.out.println("Bulunan Sonuç Metni: " + resultText);

        // Görsellik: Sonucu sarı yap
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.backgroundColor='yellow'", resultElement);

        // NOT: React kodunda 'FAIL' dönüyor olabilir, backend 'KALDI' gönderiyor olabilir.
        // İsteğine binaen 'KALDI' veya 'FAIL' kelimesini arıyoruz.
        boolean isFailOrKaldi = resultText.contains("FAIL") || resultText.contains("KALDI");
        
        if (isFailOrKaldi) {
            System.out.println("✅ TEST BAŞARILI: Araç beklendiği gibi kaldı.");
        } else {
            System.out.println("❌ TEST HATALI: Beklenen sonuç (FAIL/KALDI) bulunamadı.");
        }
        
        Assertions.assertTrue(isFailOrKaldi, "Inspection sonucu FAIL veya KALDI olmalıydı! Gelen: " + resultText);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- YARDIMCI METOTLAR ---

    /**
     * Dropdown'dan metin içeren öğeyi seçer ve Pass/Fail durumuna göre ekler.
     */
    private void addInspectionDetail(String checklistItemText, String status, String note) {
        // 1. Checklist Item Seç (Text içeriğine göre)
        WebElement checklistDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Checklist Item')]/following-sibling::select"));
        Select selectItem = new Select(checklistDropdown);
        
        // SelectByVisibleText tam eşleşme ister, biz 'contains' (içerir) mantığıyla bulacağız
        boolean itemFound = false;
        List<WebElement> options = selectItem.getOptions();
        for (WebElement option : options) {
            if (option.getText().contains(checklistItemText)) {
                selectItem.selectByVisibleText(option.getText());
                itemFound = true;
                break;
            }
        }
        
        if (!itemFound) {
            Assertions.fail("Dropdown içinde şu seçenek bulunamadı: " + checklistItemText);
        }

        // 2. Status Seç (PASS/FAIL)
        WebElement statusDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Status')]/following-sibling::select"));
        Select selectStatus = new Select(statusDropdown);
        if (status.equalsIgnoreCase("PASS")) {
            selectStatus.selectByVisibleText("PASS"); // Veya value="true"
        } else {
            selectStatus.selectByVisibleText("FAIL"); // Veya value="false"
        }

        // 3. Not Ekle
        WebElement noteInput = driver.findElement(By.xpath("//input[@placeholder='Inspector Note']"));
        noteInput.clear();
        noteInput.sendKeys(note);

        // 4. Add Butonuna Bas
        WebElement addBtn = driver.findElement(By.xpath("//button[contains(text(), 'Add') and not(contains(text(), 'Vehicle'))]")); // Add Vehicle ile karışmasın
        clickSafely(addBtn);
        
        // Eklendiğini teyit etmek için kısa bekle (Tabloya düşmesi için)
        sleep(500);
    }

    private void performLogin(String user, String pass) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text']")
        ));
        usernameField.clear();
        usernameField.sendKeys(user);

        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password']"));
        passwordField.clear();
        passwordField.sendKeys(pass);

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