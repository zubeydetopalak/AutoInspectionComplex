package selenium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Scenario01_PassInspection_EndToEndTest extends BaseScenarioSeleniumTest {

    @Test
    public void longScenario_secretaryCreatesAppointment_inspectorCompletesPass_secretarySeesCompleted() {
        String suffix = uniqueSuffix();
        String phone = "555" + suffix;
        String customerName = "Scenario01_" + suffix;
        String email = "scenario01_" + suffix + "@test.com";
        String plate = "S01" + suffix;
        String chassis = "CH" + suffix;

        openApp();
        performLogin("secretary", "password");

        secretarySearchByPhone(phone);
        secretaryRegisterCustomer(customerName, email, phone);

        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate, "Toyota", "2024", chassis);
        secretaryCreateAppointmentForPlate(plate);

        secretaryShowDetailsForPlate(plate);
        String appointmentId = secretaryReadAppointmentIdFromDetailsModal();
        String stationCode = secretaryReadAssignedStationFromDetailsModal();
        Assertions.assertFalse(stationCode.equalsIgnoreCase("Pending"), "Station should be assigned (not Pending)");
        closeModalByText("Close");

        openApp();
        performLogin("zub", "zub");

        inspectorSearchByPlate(plate);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[contains(text(), '" + plate + "')]]")));

        inspectorOpenInspectionModalFromPlateRow(plate);
        inspectorAddInspectionDetail("Fren", true, "All good - " + suffix);
        inspectorAddInspectionDetail("Far", true, "All good - " + suffix);
        inspectorClickCompleteInspection();

        WebElement actionBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + plate + "')]/..//button")
        ));
        Assertions.assertTrue(actionBtn.getText().contains("Show Inspection"), "After completion, action should be Show Inspection");

        openApp();
        performLogin("secretary", "password");

        WebElement showAllBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Show All Appointments')]"))
        );
        clickSafely(showAllBtn);
        sleep(800);

        WebElement apptRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(), '" + appointmentId + "')] and td[contains(text(), '" + plate + "')]]")
        ));
        String rowText = apptRow.getText();
        Assertions.assertTrue(rowText.contains("COMPLETED"), "Secretary should see appointment status COMPLETED after inspection completion");
    }
}
