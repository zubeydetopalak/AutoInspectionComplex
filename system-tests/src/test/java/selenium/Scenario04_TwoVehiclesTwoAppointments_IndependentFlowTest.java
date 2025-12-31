package com.zubeyde.auto.selenium.scenario;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Scenario04_TwoVehiclesTwoAppointments_IndependentFlowTest extends BaseScenarioSeleniumTest {

    @Test
    public void longScenario_oneCustomer_twoVehicles_twoAppointments_completeOnlyFirst() {
        String suffix = uniqueSuffix();
        String phone = "558" + suffix;
        String customerName = "Scenario04_" + suffix;
        String email = "scenario04_" + suffix + "@test.com";

        String plate1 = "S04A" + suffix;
        String plate2 = "S04B" + suffix;

        // ===== Secretary creates customer + 2 vehicles + 2 appointments =====
        openApp();
        performLogin("secretary", "password");
        secretarySearchByPhone(phone);
        secretaryRegisterCustomer(customerName, email, phone);

        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate1, "Toyota", "2024", "CH" + suffix + "A");
        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate2, "Ford", "2021", "CH" + suffix + "B");

        secretaryCreateAppointmentForPlate(plate1);
        secretaryCreateAppointmentForPlate(plate2);

        // ===== Inspector completes only first appointment =====
        openApp();
        performLogin("zub", "zub");

        inspectorSearchByPlate(plate1);
        inspectorOpenInspectionModalFromPlateRow(plate1);
        inspectorAddInspectionDetail("Far", true, "OK - " + suffix);
        inspectorClickCompleteInspection();

        // Plate1 should be completed -> Show Inspection
        WebElement btn1 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + plate1 + "')]/..//button")
        ));
        Assertions.assertTrue(btn1.getText().contains("Show Inspection"), "Plate1 should show Show Inspection");

        // Plate2 still pending (no inspection started) -> Start Inspection
        inspectorSearchByPlate(plate2);
        WebElement btn2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + plate2 + "')]/..//button")
        ));
        Assertions.assertTrue(btn2.getText().contains("Start Inspection"), "Plate2 should still show Start Inspection");
    }
}
