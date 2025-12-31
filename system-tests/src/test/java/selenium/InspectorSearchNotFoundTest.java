package com.zubeyde.auto.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InspectorSearchNotFoundTest {

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless"); // Jenkins için ilerde açacağız

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testSearchNonExistingPlate() {
        // --- ADIM 1: GİRİŞ YAP (Login) ---
        driver.get("http://localhost:3000");

        // Giriş bilgilerini doldur
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("zub");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("zub");
        driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]")).click();

        // Dashboard'un açılmasını bekle
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("inspector-dashboard"));


        // --- ADIM 2: OLMAYAN BİR PLAKA ARA (99) ---
        
        // Arama kutusunu bul
        WebElement searchInput = driver.findElement(By.xpath("//input[@placeholder='Search by Plate Code']"));
        
        // Kutuyu temizle ve '99' yaz
        searchInput.clear();
        searchInput.sendKeys("99");

        // Search butonuna tıkla
        WebElement searchBtn = driver.findElement(By.xpath("//button[text()='Search']"));
        searchBtn.click();


        // --- ADIM 3: UYARI MESAJINI DOĞRULA ---

        // React kodunda: <p>No appointments found.</p> elementinin görünmesini bekle
        WebElement notFoundMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(), 'No appointments found.')]")
        ));

        // Mesaj görünür durumda mı kontrol et
        assertTrue(notFoundMessage.isDisplayed(), "Uyarı mesajı ekranda görünmedi!");
        
        // Konsola bilgi bas (İsteğe bağlı)
        System.out.println("Test Başarılı: '" + notFoundMessage.getText() + "' mesajı görüldü.");
    }
}