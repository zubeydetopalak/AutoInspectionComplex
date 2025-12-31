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

public class InspectorSaveTest {

    private WebDriver driver;
    private WebDriverWait wait;
    
    // Hedef Plaka
    private final String targetPlate = "90ABC123";

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
    public void testSaveButtonKeepsStatusPending() {
        driver.get("http://localhost:3000");
        
        // 1. GİRİŞ YAP
        System.out.println("--- Adım 1: Inspector Girişi ---");
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

        // 3. START/UPDATE INSPECTION BUTONUNA BAS (Modalı Aç)
        System.out.println("--- Adım 3: İnceleme Modalı Açılıyor ---");
        
        // Buton "Start Inspection" veya daha önce dokunulduysa "Update Inspection" olabilir.
        // Amaç modalı açmak.
        WebElement actionBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button")
        ));
        clickSafely(actionBtn);

        // Modalı bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));

        // 4. BİR DETAY EKLE (Örn: Far Ayarları -> FAIL)
        System.out.println("--- Adım 4: Detay Ekleniyor (Değişiklik yapılıyor) ---");
        addInspectionDetail("Far Ayarları", "FAIL", "Ayarsız (Kaydet testi)");

        // 5. 'COMPLETE' DEĞİL, 'KAYDET' BUTONUNA BAS
        System.out.println("--- Adım 5: 'Kaydet' Butonuna Basılıyor (Tamamla DEĞİL) ---");
        
        WebElement kaydetBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//button[contains(text(), 'Kaydet')]")
        ));
        
        // Görsellik: Kaydet butonunu vurgula
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid orange'", kaydetBtn);
        
        clickSafely(kaydetBtn);
        
        // Modalın kapanmasını bekle (React kodunda closeModal() çağrılıyor)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
        sleep(1000); // Tablonun güncellenmesi için kısa bekleme

        // 6. DOĞRULAMA: BUTON "UPDATE INSPECTION" OLMALI
        System.out.println("--- Adım 6: Buton Metni Kontrol Ediliyor ---");
        
        // Tablodaki butonu tekrar bul
        WebElement statusButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button")
        ));
        
        String buttonText = statusButton.getText();
        System.out.println("Tablodaki Buton Metni: " + buttonText);
        
        // Görsellik: Butonu mavi yap
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.backgroundColor='cyan'", statusButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.color='black'", statusButton);

        // ASSERTION
        // Beklenen durum: Buton "Update Inspection" yazmalı.
        // Eğer "Show Inspection" yazıyorsa işlem tamamlanmış (Complete) demektir, bu hatadır.
        // Eğer "Start Inspection" yazıyorsa kayıt olmamış demektir.
        
        boolean isPending = buttonText.contains("Update Inspection");
        
        if (isPending) {
            System.out.println("✅ TEST BAŞARILI: İşlem tamamlanmadı, statü hala 'Update Inspection'.");
        } else {
            System.out.println("❌ TEST HATALI: Beklenen 'Update Inspection', Gelen: " + buttonText);
        }
        
        Assertions.assertTrue(isPending, "Kaydet'e basınca buton 'Update Inspection' olmalı! Gelen: " + buttonText);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- YARDIMCI METOTLAR ---

    private void addInspectionDetail(String checklistItemText, String status, String note) {
        // Item Seç
        WebElement checklistDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Checklist Item')]/following-sibling::select"));
        Select selectItem = new Select(checklistDropdown);
        
        boolean itemFound = false;
        List<WebElement> options = selectItem.getOptions();
        for (WebElement option : options) {
            if (option.getText().toLowerCase().contains(checklistItemText.toLowerCase())) {
                selectItem.selectByVisibleText(option.getText());
                itemFound = true;
                break;
            }
        }
        
        // Eğer bulamazsa ilkini seç (Test patlamasın diye fallback)
        if (!itemFound && options.size() > 1) {
            selectItem.selectByIndex(1);
        }

        // Status Seç
        WebElement statusDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Status')]/following-sibling::select"));
        Select selectStatus = new Select(statusDropdown);
        if (status.equalsIgnoreCase("PASS")) {
            selectStatus.selectByVisibleText("PASS"); 
        } else {
            selectStatus.selectByVisibleText("FAIL");
        }

        // Not Ekle
        driver.findElement(By.xpath("//input[@placeholder='Inspector Note']")).sendKeys(note);

        // Ekle Butonu
        WebElement addBtn = driver.findElement(By.xpath("//button[contains(text(), 'Add') and not(contains(text(), 'Vehicle'))]"));
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
        
        clickSafely(driver.findElement(By.xpath("//button[contains(text(), 'Sign In') or contains(text(), 'Login')]")));
        
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