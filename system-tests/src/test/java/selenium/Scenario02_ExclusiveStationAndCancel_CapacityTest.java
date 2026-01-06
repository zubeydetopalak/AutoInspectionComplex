package selenium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Scenario02_ExclusiveStationAndCancel_CapacityTest extends BaseScenarioSeleniumTest {

    @Test
    public void longScenario_bmwAppointment_isExclusive_thenCancel_restoresStationCapacity() {
        String suffix = uniqueSuffix();
        String phone = "556" + suffix;
        String customerName = "Scenario02_" + suffix;
        String email = "scenario02_" + suffix + "@test.com";
        String plate = "BMW" + suffix;
        String chassis = "CH" + suffix;

        openApp();
        performLogin("secretary", "password");

        secretarySearchByPhone(phone);
        secretaryRegisterCustomer(customerName, email, phone);

        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate, "BMW", "2023", chassis);

        secretaryCreateAppointmentForPlate(plate);
        secretaryShowDetailsForPlate(plate);
        String stationCode = secretaryReadAssignedStationFromDetailsModal();

        Assertions.assertTrue(
                driver.getPageSource().contains("Exclusive (BMW)"),
                "BMW vehicle should be routed to an exclusive station (Exclusive (BMW))"
        );

        closeModalByText("Close");

        int capacityAfterCreate = readStationCapacityFromGrid(stationCode);

        safeSwitchTab("Customers & Appointments", "Customer Management");
        secretaryShowDetailsForPlate(plate);
        secretaryUpdateStatusInDetailsModal("CANCELLED");
        closeModalByText("Close");

        int capacityAfterCancel = readStationCapacityFromGrid(stationCode);
        Assertions.assertEquals(capacityAfterCreate + 1, capacityAfterCancel, "Capacity should restore after cancelling appointment");
    }
}
