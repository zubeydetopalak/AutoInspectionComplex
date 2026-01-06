package selenium;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class BaseScenarioSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected final String baseUrl = "http://localhost:3000";

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUpBase() {
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
    }

    @AfterEach
    public void tearDownBase() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openApp() {
        driver.get(baseUrl);
        handlePotentialAlert();
    }

    protected void performLogin(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text']")
        ));
        usernameField.clear();
        usernameField.sendKeys(username);

        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password']"));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'Sign In') or contains(text(), 'Login')]"));
        clickSafely(loginBtn);

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("dashboard"),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Auto Service')]"))
        ));
    }

    protected void safeSwitchTab(String tabName, String expectedHeader) {
        String xpath = "//li[contains(text(), '" + tabName + "')]";
        try {
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            clickSafely(tab);
        } catch (Exception e) {
            WebElement tab = driver.findElement(By.xpath(xpath));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
        }
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h2"), expectedHeader));
    }

    protected void secretarySearchByPhone(String phone) {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Enter Phone Number']")
        ));
        searchInput.clear();
        searchInput.sendKeys(phone);
        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(searchBtn);
    }

    protected void secretaryRegisterCustomer(String name, String email, String phone) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(text(), 'Register New Customer')]")
        ));

        WebElement nameInput = driver.findElement(By.xpath("//label[contains(text(), 'Name')]/following-sibling::input"));
        nameInput.clear();
        nameInput.sendKeys(name);

        WebElement emailInput = driver.findElement(By.xpath("//label[contains(text(), 'Email')]/following-sibling::input"));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement phoneInput = driver.findElement(By.xpath("//label[contains(text(), 'Phone')]/following-sibling::input"));
        if (phoneInput.getAttribute("value") == null || phoneInput.getAttribute("value").isEmpty()) {
            phoneInput.sendKeys(phone);
        }

        WebElement registerBtn = driver.findElement(By.xpath("//button[contains(text(), 'Register Customer')]"));
        clickSafely(registerBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Customer Info')]")));
    }

    protected void secretaryOpenAddVehicleForm() {
        WebElement addVehicleBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Add Vehicle')]")
        ));
        clickSafely(addVehicleBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(), 'New Vehicle')]")));
    }

    protected void secretaryCreateVehicle(String plate, String brandName, String modelYear, String chassisNumber) {
        WebElement plateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[contains(text(), 'Plate Code')]/following-sibling::input")
        ));
        plateInput.clear();
        plateInput.sendKeys(plate);

        WebElement brandSelectElement = driver.findElement(By.xpath("//label[contains(text(), 'Brand')]/following-sibling::select"));
        Select brandSelect = new Select(brandSelectElement);
        try {
            brandSelect.selectByVisibleText(brandName);
        } catch (Exception e) {
            try {
                brandSelect.selectByIndex(1);
            } catch (Exception ignore) {

            }
        }

        WebElement modelInput = driver.findElement(By.xpath("//label[contains(text(), 'Model Year')]/following-sibling::input"));
        modelInput.clear();
        modelInput.sendKeys(modelYear);

        WebElement chassisInput = driver.findElement(By.xpath("//label[contains(text(), 'Chassis Number')]/following-sibling::input"));
        chassisInput.clear();
        chassisInput.sendKeys(chassisNumber);

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(text(), 'Save Vehicle')]"));
        clickSafely(saveBtn);
        handlePotentialAlert();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Add Vehicle')]")));
    }

    protected void secretaryCreateAppointmentForPlate(String plate) {
        WebElement newApptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//td[contains(text(), '" + plate + "')]/..//button[contains(text(), 'New Appointment')]")
        ));
        clickSafely(newApptBtn);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Appointment created"));
        sleep(800);
    }

    protected void secretaryShowDetailsForPlate(String plate) {
        WebElement detailsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//td[contains(text(), '" + plate + "')]/..//button[contains(text(), 'Show Details')]")
        ));
        clickSafely(detailsBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Appointment Details')]")));
    }

    protected String secretaryReadAssignedStationFromDetailsModal() {
        WebElement stationLine = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[strong[contains(text(), 'Assigned Station')]]")
        ));
        String text = stationLine.getText();

        int idx = text.indexOf(":");
        return idx >= 0 ? text.substring(idx + 1).trim() : text.trim();
    }

    protected String secretaryReadAppointmentIdFromDetailsModal() {
        WebElement idLine = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[strong[contains(text(), 'Appointment ID')]]")
        ));
        String text = idLine.getText();
        int idx = text.indexOf(":");
        return idx >= 0 ? text.substring(idx + 1).trim() : text.trim();
    }

    protected void secretaryUpdateStatusInDetailsModal(String statusValue) {
        WebElement selectEl = driver.findElement(By.tagName("select"));
        Select statusSelect = new Select(selectEl);
        statusSelect.selectByValue(statusValue);
        sleep(300);
        WebElement updateBtn = driver.findElement(By.xpath("//button[contains(text(), 'Update')]"));
        clickSafely(updateBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Appointment status updated')]")));
    }

    protected void closeModalByText(String buttonText) {
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), '" + buttonText + "')]")
        ));
        clickSafely(closeBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
    }

    protected int readStationCapacityFromGrid(String stationCode) {
        safeSwitchTab("Station Status", "Station Overview");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("stations-grid")));
        String stationCardXpath = "//div[contains(@class, 'station-card')][.//h3[text()='" + stationCode + "']]";
        WebElement stationCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(stationCardXpath)));
        WebElement capacityLine = stationCard.findElement(By.xpath(".//p[contains(., 'Capacity:')]"));
        String text = capacityLine.getText();
        String numberPart = text.split(":")[1].trim();
        return Integer.parseInt(numberPart);
    }

    protected void inspectorSearchByPlate(String plate) {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Search by Plate Code']")
        ));
        searchInput.clear();
        searchInput.sendKeys(plate);

        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(), 'Search')]"));
        clickSafely(searchBtn);
        sleep(800);
    }

    protected void inspectorOpenInspectionModalFromPlateRow(String plate) {
        WebElement actionBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//td[contains(text(), '" + plate + "')]/..//button")
        ));
        clickSafely(actionBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Inspection for')]")));
    }

    protected void inspectorAddInspectionDetail(String checklistContainsText, boolean passed, String note) {
        WebElement checklistDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Checklist Item')]/following-sibling::select"));
        Select selectItem = new Select(checklistDropdown);
        boolean selected = false;
        for (WebElement opt : selectItem.getOptions()) {
            if (opt.getText().toLowerCase().contains(checklistContainsText.toLowerCase())) {
                selectItem.selectByVisibleText(opt.getText());
                selected = true;
                break;
            }
        }
        if (!selected) {

            if (selectItem.getOptions().size() > 1) {
                selectItem.selectByIndex(1);
            }
        }

        WebElement statusDropdown = driver.findElement(By.xpath("//label[contains(text(), 'Status')]/following-sibling::select"));
        Select selectStatus = new Select(statusDropdown);
        selectStatus.selectByVisibleText(passed ? "PASS" : "FAIL");

        WebElement noteInput = driver.findElement(By.xpath("//input[@placeholder='Inspector Note']"));
        noteInput.clear();
        noteInput.sendKeys(note);

        WebElement addBtn = driver.findElement(By.xpath("//button[contains(text(), 'Add') and not(contains(text(), 'Vehicle'))]"));
        clickSafely(addBtn);
        sleep(300);
    }

    protected void inspectorClickKaydet() {
        WebElement kaydetBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Kaydet')]")
        ));
        clickSafely(kaydetBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
        sleep(800);
    }

    protected void inspectorClickCompleteInspection() {
        WebElement completeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Complete Inspection')]")
        ));
        clickSafely(completeBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-content")));
        sleep(1000);
    }

    protected String uniqueSuffix() {
        String millis = String.valueOf(System.currentTimeMillis());
        String last = millis.length() > 6 ? millis.substring(millis.length() - 6) : millis;
        int extra = ThreadLocalRandom.current().nextInt(100, 999);
        return last + extra;
    }

    protected void clickSafely(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected void handlePotentialAlert() {
        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (NoAlertPresentException ignored) {
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
