package com.zubeyde.auto.selenium;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FrontendTest {

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        // 1. Chrome sürücüsünü otomatik ayarlar
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        // 2. Tarayıcı ayarları
        ChromeOptions options = new ChromeOptions();
        
        // ÖNEMLİ: Jenkins'te ekran olmadığı için bu satırı ilerde açacağız.
        // Şimdilik kendi gözünle görmek için kapalı kalsın (// koyduk).
        // options.addArguments("--headless"); 
        
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterEach
    public void teardown() {
        // 3. Test bitince tarayıcıyı kapatır
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testWebsiteOpens() {
        // 4. React sitesine git (Frontend'in 3000 portunda çalıştığından emin ol)
        driver.get("http://localhost:3000");

        // 5. Basit bir kontrol: Sayfa başlığını al
        String title = driver.getTitle();
        System.out.println("Site Başlığı: " + title);
        
        // Başlığın boş olmadığını doğrula
        assertNotNull(title);
    }
}