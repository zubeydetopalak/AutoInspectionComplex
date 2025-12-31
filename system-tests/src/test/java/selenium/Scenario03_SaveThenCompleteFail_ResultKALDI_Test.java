package selenium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Scenario03_SaveThenCompleteFail_ResultKALDI_Test extends BaseScenarioSeleniumTest {

    @Test
    public void longScenario_secretaryCreatesAppointment_inspectorSavesThenCompletesFail_showsShowInspection() {
        String suffix = uniqueSuffix();
        String phone = "557" + suffix;
        String customerName = "Scenario03_" + suffix;
        String email = "scenario03_" + suffix + "@test.com";
        String plate = "S03" + suffix;
        String chassis = "CH" + suffix;

        // ===== Secretary creates appointment =====
        openApp();
        performLogin("secretary", "password");

        secretarySearchByPhone(phone);
        secretaryRegisterCustomer(customerName, email, phone);
        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate, "Honda", "2022", chassis);
        secretaryCreateAppointmentForPlate(plate);

        // ===== Inspector starts, adds AGIR_KUSUR FAIL, saves (Kaydet), then completes =====
        openApp();
        performLogin("zub", "zub");

        inspectorSearchByPlate(plate);
        inspectorOpenInspectionModalFromPlateRow(plate);

        // One heavy fault (AGIR_KUSUR) failed should force "KALDI" in backend
        inspectorAddInspectionDetail("Fren", false, "Heavy fault - " + suffix);
        inspectorClickKaydet();

        // After Kaydet, button should be Update Inspection (still pending)
        WebElement actionBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + plate + "')]/..//button")
        ));
        Assertions.assertTrue(actionBtn.getText().contains("Update Inspection"), "After Kaydet, action should be Update Inspection");

        // Re-open, add more detail, then complete
        inspectorOpenInspectionModalFromPlateRow(plate);
        inspectorAddInspectionDetail("Lastik", true, "Second check - " + suffix);
        inspectorClickCompleteInspection();

        WebElement finalBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + plate + "')]/..//button")
        ));
        Assertions.assertTrue(finalBtn.getText().contains("Show Inspection"), "After completion, action should be Show Inspection");
    }
}
