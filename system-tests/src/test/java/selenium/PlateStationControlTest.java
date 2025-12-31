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

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PlateStationControlTest {

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless"); // Jenkins için sonra açacağız

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
    public void testSearchPlateAndVerifyStation() {
        // --- ADIM 1: GİRİŞ YAP (Login Flow) ---
        driver.get("http://localhost:3000");

        // Kullanıcı adı ve şifre gir (Inspector rolü)
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("zub");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("zub");
        
        // Giriş butonuna tıkla
        driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]")).click();

        // Dashboard'un yüklenmesini bekle
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("inspector-dashboard"));


        // --- ADIM 2: ARAMA YAP (Search Flow) ---
        
        // Arama kutusunu bul (Placeholder özelliğinden)
        WebElement searchInput = driver.findElement(By.xpath("//input[@placeholder='Search by Plate Code']"));
        
        // Plakayı yaz
        String targetPlate = "34ABC123";
        searchInput.clear();
        searchInput.sendKeys(targetPlate);

        // 'Search' butonuna tıkla
        WebElement searchBtn = driver.findElement(By.xpath("//button[text()='Search']"));
        searchBtn.click();


        // --- ADIM 3: SONUCU DOĞRULA (Verification) ---

        // Tablonun güncellenmesini bekle. 
        // Mantık şu: İçinde "34ABC123" yazan bir satır (tr) görünene kadar bekle.
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(), '" + targetPlate + "')]]")
        ));

        // İstasyon sütununu bul (XPath Açıklaması aşağıda*)
        // "34ABC123" içeren satırın 4. sütununu (Station) getir.
        WebElement stationCell = driver.findElement(
                By.xpath("//tr[td[contains(text(), '" + targetPlate + "')]]/td[4]")
        );

        String stationText = stationCell.getText();
        System.out.println("Bulunan İstasyon: " + stationText);

        // Beklenen değer ile karşılaştır
        assertEquals("ST-GEN-01", stationText, "İstasyon kodu eşleşmedi!");
    }
}
