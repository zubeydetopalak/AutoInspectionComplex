package selenium;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InspectorSaveTest {

    private WebDriver driver;
    private WebDriverWait wait;

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

        System.out.println("--- Adım 3: İnceleme Modalı Açılıyor ---");

        WebElement actionBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button")
        ));
        clickSafely(actionBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));

        System.out.println("--- Adım 4: Detay Ekleniyor (Değişiklik yapılıyor) ---");
        addInspectionDetail("Far Ayarları", "FAIL", "Ayarsız (Kaydet testi)");

        System.out.println("--- Adım 5: 'Kaydet' Butonuna Basılıyor (Tamamla DEĞİL) ---");
        
        WebElement kaydetBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//button[contains(text(), 'Kaydet')]")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid orange'", kaydetBtn);
        
        clickSafely(kaydetBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
        sleep(1000); // Tablonun güncellenmesi için kısa bekleme

        System.out.println("--- Adım 6: Buton Metni Kontrol Ediliyor ---");

        WebElement statusButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//td[contains(text(), '" + targetPlate + "')]/..//button")
        ));
        
        String buttonText = statusButton.getText();
        System.out.println("Tablodaki Buton Metni: " + buttonText);

        ((JavascriptExecutor) driver).executeScript("arguments[0].style.backgroundColor='cyan'", statusButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.color='black'", statusButton);
        
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


    private void addInspectionDetail(String checklistItemText, String status, String note) {
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

        if (!itemFound && options.size() > 1) {
            selectItem.selectByIndex(1);
        }

        WebElement statusDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Status')]/following-sibling::select"));
        Select selectStatus = new Select(statusDropdown);
        if (status.equalsIgnoreCase("PASS")) {
            selectStatus.selectByVisibleText("PASS"); 
        } else {
            selectStatus.selectByVisibleText("FAIL");
        }

        driver.findElement(By.xpath("//input[@placeholder='Inspector Note']")).sendKeys(note);

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