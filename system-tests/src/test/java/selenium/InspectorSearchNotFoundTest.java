package selenium;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        driver.get("http://localhost:3000");

        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("zub");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("zub");
        driver.findElement(By.xpath("//button[contains(text(), 'Sign In')]")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("inspector-dashboard"));

        WebElement searchInput = driver.findElement(By.xpath("//input[@placeholder='Search by Plate Code']"));

        searchInput.clear();
        searchInput.sendKeys("99");

        WebElement searchBtn = driver.findElement(By.xpath("//button[text()='Search']"));
        searchBtn.click();

        WebElement notFoundMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(), 'No appointments found.')]")
        ));

        assertTrue(notFoundMessage.isDisplayed(), "Uyarı mesajı ekranda görünmedi!");

        System.out.println("Test Başarılı: '" + notFoundMessage.getText() + "' mesajı görüldü.");
    }
}