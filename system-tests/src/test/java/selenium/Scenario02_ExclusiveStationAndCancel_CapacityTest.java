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

        // Create Customer & BMW Vehicle
        secretarySearchByPhone(phone);
        secretaryRegisterCustomer(customerName, email, phone);

        secretaryOpenAddVehicleForm();
        secretaryCreateVehicle(plate, "BMW", "2023", chassis);

        // Create appointment and capture assigned station
        secretaryCreateAppointmentForPlate(plate);
        secretaryShowDetailsForPlate(plate);
        String stationCode = secretaryReadAssignedStationFromDetailsModal();

        // Exclusive station should show "Exclusive (BMW)" line in modal
        Assertions.assertTrue(
                driver.getPageSource().contains("Exclusive (BMW)"),
                "BMW vehicle should be routed to an exclusive station (Exclusive (BMW))"
        );

        closeModalByText("Close");

        // Measure capacity after create (capacity already decremented by backend)
        int capacityAfterCreate = readStationCapacityFromGrid(stationCode);

        // Go back, cancel appointment via details modal
        safeSwitchTab("Customers & Appointments", "Customer Management");
        secretaryShowDetailsForPlate(plate);
        secretaryUpdateStatusInDetailsModal("CANCELLED");
        closeModalByText("Close");

        // Capacity should restore by +1 after CANCELLED
        int capacityAfterCancel = readStationCapacityFromGrid(stationCode);
        Assertions.assertEquals(capacityAfterCreate + 1, capacityAfterCancel, "Capacity should restore after cancelling appointment");
    }
}
