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

public class InspectorLoginTest {

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
    public void testInspectorLoginFlow() {
        // 1. React Uygulamasına Git
        driver.get("http://localhost:3000");

        // 2. Login Ekranındaki Elementleri Bul
        // React koduna göre: type="text" username, type="password" şifre.
        WebElement usernameInput = driver.findElement(By.cssSelector("input[type='text']"));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));

        // Butonu bul: İçinde 'Sign In' yazan buton
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]"));

        // 3. Bilgileri Doldur ve Tıkla (Zub / Zub)
        usernameInput.clear();
        usernameInput.sendKeys("zub");

        passwordInput.clear();
        passwordInput.sendKeys("zub");

        loginBtn.click();

        // 4. BEKLEME (Wait) - Çok Önemli!
        // Sayfanın değişmesi ve Dashboard'un yüklenmesi için beklemeliyiz.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // InspectorDashboard.jsx içindeki <h2>Inspection Management</h2> yazısını bekliyoruz
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(), 'Inspection Management')]")
        ));

        // 5. Doğrulama (Assertion)
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Giriş sonrası URL: " + currentUrl);

        // URL'in '/inspector-dashboard' içerdiğini doğrula
        assertTrue(currentUrl.contains("inspector-dashboard"), "Login başarısız! Dashboard'a gidilemedi.");
    }
}