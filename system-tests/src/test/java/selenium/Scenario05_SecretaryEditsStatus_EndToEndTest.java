package selenium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Scenario05_SecretaryEditsStatus_EndToEndTest extends BaseScenarioSeleniumTest {

    @Test
    public void longScenario_createAppointment_thenEditStatusCompleted_thenVerifyBadgeInAllAppointments() {
        String suffix = uniqueSuffix();
        String phone = "559" + suffix;
        String customerName = "Scenario05_" + suffix;
        String email = "scenario05_" + suffix + "@test.com";
        String plate = "S05" + suffix;
        String chassis = "CH" + suffix;

        openApp();
        performLogin("secretary", "password");

        secretarySearchByPhone(phone);
        secretaryRegisterCustomer(customerName, email, phone);
        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate, "Volkswagen", "2020", chassis);
        secretaryCreateAppointmentForPlate(plate);

        // Show All Appointments -> open modal via Edit Status -> set COMPLETED
        WebElement showAllBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Show All Appointments')]"))
        );
        clickSafely(showAllBtn);
        sleep(800);

        WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(), '" + plate + "')]]")
        ));
        WebElement editBtn = row.findElement(By.xpath(".//button[contains(text(), 'Edit Status')]"));
        clickSafely(editBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));

        secretaryUpdateStatusInDetailsModal("COMPLETED");
        closeModalByText("Close");

        // Verify row shows COMPLETED badge
        WebElement rowAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(), '" + plate + "')] and td//*[contains(text(), 'COMPLETED')]]")
        ));
        Assertions.assertTrue(rowAfter.isDisplayed(), "Appointment should show COMPLETED in All Appointments table");
    }
}
