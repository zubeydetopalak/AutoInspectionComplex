package selenium;


import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import io.github.bonigarcia.wdm.WebDriverManager;


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
        driver.get("http://localhost:3000");

        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("zub");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("zub");

        driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("inspector-dashboard"));

        WebElement searchInput = driver.findElement(By.xpath("//input[@placeholder='Search by Plate Code']"));

        String targetPlate = "34ABC123";
        searchInput.clear();
        searchInput.sendKeys(targetPlate);

        WebElement searchBtn = driver.findElement(By.xpath("//button[text()='Search']"));
        searchBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(), '" + targetPlate + "')]]")
        ));

        WebElement stationCell = driver.findElement(
                By.xpath("//tr[td[contains(text(), '" + targetPlate + "')]]/td[4]")
        );

        String stationText = stationCell.getText();
        System.out.println("Bulunan İstasyon: " + stationText);

        assertEquals("ST-GEN-01", stationText, "İstasyon kodu eşleşmedi!");
    }
}
