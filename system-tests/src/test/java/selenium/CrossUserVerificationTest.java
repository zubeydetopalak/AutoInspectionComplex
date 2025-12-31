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
import java.util.Random;

public class CrossUserVerificationTest {

    private WebDriver driver;
    private WebDriverWait wait;
    
    // Test boyunca takip edeceğimiz dinamik veriler
    private String randomPhone;   // Arama yapılacak rastgele telefon (ID yerine)
    private String targetPlate;   // Oluşturulacak Plaka
    private String customerName;  // Müşteri Adı

    @BeforeEach
    public void setUp() {
        // --- 1. AYARLAR ---
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // --- 2. RASTGELE VERİ OLUŞTURMA ---
        Random rand = new Random();
        int randNum = rand.nextInt(9000) + 1000;
        
        this.randomPhone = "555" + randNum; 
        this.targetPlate = "34 LEV " + randNum;
        this.customerName = "Musteri" + randNum;
        
        System.out.println("TEST SENARYOSU VERİLERİ:");
        System.out.println("Rastgele Telefon: " + randomPhone);
        System.out.println("Hedef Plaka: " + targetPlate);
    }

    @Test
    public void testSecretaryToInspectorFlow() {
        driver.get("http://localhost:3000");
        handlePotentialAlert();

        // ==========================================
        // BÖLÜM 1: SECRETARY İŞLEMLERİ
        // ==========================================
        
        System.out.println("--- Adım 1: Secretary Login ---");
        performLogin("secretary", "password");

        // --- Adım 2: Telefon Numarası Arama (Secretary Dashboard) ---
        System.out.println("--- Adım 2: Telefon aranıyor (" + randomPhone + ") ---");
        
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Enter Phone Number']")
        ));
        searchInput.clear();
        searchInput.sendKeys(randomPhone);
        
        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(searchBtn);

        // --- Adım 3: Register Customer ---
        System.out.println("--- Adım 3: Müşteri bulunamadı, kayıt formu dolduruluyor ---");
        
        // 'Register New Customer' başlığının görünmesini bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h3[contains(text(), 'Register New Customer')]")
        ));
        
        fillRegisterForm(customerName, customerName + "@test.com", randomPhone);

        // --- Adım 4: Araç Ekleme ---
        System.out.println("--- Adım 4: Araç ekleniyor (" + targetPlate + ") ---");
        
        WebElement addVehicleBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//button[contains(text(), 'Add Vehicle')]")
        ));
        clickSafely(addVehicleBtn);
        
        fillVehicleForm(targetPlate, "2023", "CHASSIS" + randomPhone);
        
        System.out.println("Araç başarıyla eklendi.");

        // --- Adım 5: New Appointment Butonuna Bas ---
        System.out.println("--- Adım 5: Randevu oluşturuluyor ---");
        
        // Tabloda plakayı bul ve yanındaki New Appointment butonuna tıkla
        WebElement newApptBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button[contains(text(), 'New Appointment')]")
        ));
        clickSafely(newApptBtn);
        
        // Başarı mesajını bekle
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Appointment created"));
        sleep(1000); // İşlemin veritabanına oturması için kısa bir bekleme

        // ==========================================
        // BÖLÜM 2: KULLANICI DEĞİŞİMİ
        // ==========================================
        
        System.out.println("--- Adım 6: Çıkış yapılıyor ve Zub kullanıcısına geçiliyor ---");
        
        // Güvenli çıkış için sayfayı yenileyip login ekranına zorluyoruz (Logout butonu aramadan)
        driver.get("http://localhost:3000");
        handlePotentialAlert();
        
        // Zub Login
        performLogin("zub", "zub");

        // ==========================================
        // BÖLÜM 3: ZUB (INSPECTOR) DOĞRULAMASI
        // ==========================================

        System.out.println("--- Adım 7: Zub ekranında plaka (" + targetPlate + ") aranıyor ---");
        
        // INSPECTOR DASHBOARD: Arama kutusunu bul
        // React kodunda: placeholder="Search by Plate Code"
        WebElement inspectorSearchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Search by Plate Code']")
        ));
        
        inspectorSearchInput.clear();
        inspectorSearchInput.sendKeys(targetPlate);
        
        // Inspector Dashboard'daki 'Search' butonuna bas
        WebElement inspectorSearchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(inspectorSearchBtn);
        
        // React filtreleme işlemi için kısa bekleme
        sleep(1000); 

        // --- DOĞRULAMA ---
        try {
            // Tabloda plakanın olduğu hücreyi bul
            WebElement resultCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + targetPlate + "')]")
            ));
            
            // Görsellik: Bulunan elementi sarı yap
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].style.border='5px solid gold'", resultCell);
            js.executeScript("arguments[0].style.backgroundColor='yellow'", resultCell);

            System.out.println("✅ TEST BAŞARILI! Zub kullanıcısı, Secretary'nin eklediği " + targetPlate + " plakasını görüntüledi.");
            Assertions.assertTrue(resultCell.isDisplayed());
            
            // Ekstra Doğrulama: 'Start Inspection' butonunun geldiğini de kontrol et
            WebElement startInspectionBtn = resultCell.findElement(By.xpath("./..//button[contains(text(), 'Start Inspection') or contains(text(), 'Show Inspection')]"));
            Assertions.assertTrue(startInspectionBtn.isDisplayed(), "Start Inspection butonu görünmeli");

        } catch (TimeoutException e) {
            System.out.println("❌ HATA: Zub kullanıcısı plakayı bulamadı!");
            Assertions.fail("Plaka arama sonuçlarında çıkmadı: " + targetPlate);
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- YARDIMCI METOTLAR ---

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
        
        // Giriş sonrası dashboard'u bekle
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("dashboard"),
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Auto Service')]"))
        ));
    }

    private void fillRegisterForm(String name, String email, String phone) {
        // Label'a göre input bulma (Secretary Formu)
        WebElement nameInput = driver.findElement(By.xpath("//label[contains(text(), 'Name')]/following-sibling::input"));
        nameInput.sendKeys(name);
        
        WebElement emailInput = driver.findElement(By.xpath("//label[contains(text(), 'Email')]/following-sibling::input"));
        emailInput.sendKeys(email);
        
        WebElement phoneInput = driver.findElement(By.xpath("//label[contains(text(), 'Phone')]/following-sibling::input"));
        if(phoneInput.getAttribute("value").isEmpty()) {
            phoneInput.sendKeys(phone);
        }

        WebElement registerBtn = driver.findElement(By.xpath("//button[contains(text(), 'Register Customer')]"));
        clickSafely(registerBtn);
        
        // Kayıt sonrası 'Customer Info' kartının gelmesini bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Customer Info')]")));
    }

    private void fillVehicleForm(String plate, String modelYear, String chassis) {
        // 1. Plaka
        WebElement plateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[contains(text(), 'Plate Code')]/following-sibling::input")
        ));
        plateInput.sendKeys(plate);

        // 2. Marka (Select)
        WebElement brandSelectElement = driver.findElement(By.xpath("//label[contains(text(), 'Brand')]/following-sibling::select"));
        Select brandSelect = new Select(brandSelectElement);
        try {
            brandSelect.selectByIndex(1); // İlk markayı seç
        } catch (Exception e) {
            System.out.println("Marka seçilemedi, listede eleman olmayabilir!");
        }

        // 3. Model Yılı
        WebElement modelInput = driver.findElement(By.xpath("//label[contains(text(), 'Model Year')]/following-sibling::input"));
        modelInput.sendKeys(modelYear);

        // 4. Şasi No
        WebElement chassisInput = driver.findElement(By.xpath("//label[contains(text(), 'Chassis Number')]/following-sibling::input"));
        chassisInput.sendKeys(chassis);

        // Kaydet
        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(text(), 'Save Vehicle')]"));
        clickSafely(saveBtn);
        
        handlePotentialAlert();
        
        // Modalın kapanmasını ve Add Vehicle butonunun geri gelmesini bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Add Vehicle']")));
    }

    private void clickSafely(WebElement element) {
        handlePotentialAlert();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }

    private void handlePotentialAlert() {
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // Alert yoksa sorun yok
        }
    }
    
    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }
}