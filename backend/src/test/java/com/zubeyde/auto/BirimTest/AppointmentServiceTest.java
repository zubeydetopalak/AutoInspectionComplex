package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.*;
import com.zubeyde.auto.repository.AppointmentRepository;
import com.zubeyde.auto.repository.StationRepository;
import com.zubeyde.auto.repository.VehicleRepository;
import com.zubeyde.auto.service.AppointmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;

import static com.zubeyde.auto.entity.AppointmentStatus.COMPLETED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class AppointmentServiceTest {
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAppointment() {

        Customer customer = new Customer(1L, "Zübeyde", "", "5551234567");
        Brand brand = new Brand(2L, "BMW", "Alman Malı");
        Vehicle vehicle = new Vehicle(3L, brand, customer);
        Station station = new Station(4L, brand);
        Appointment appointment = new Appointment(11L, AppointmentStatus.PENDING, vehicle, station);

        when(vehicleRepository.findByPlateCode("34ABC34")).thenReturn(java.util.Optional.of(vehicle));

        when(stationRepository.findAll()).thenReturn(java.util.Arrays.asList(station));

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        Appointment appointment1 = appointmentService.createAppointment("34ABC34");
        Assertions.assertEquals(appointment1.getId(), 11L);
        Assertions.assertEquals(appointment1.getVehicle().getBrand().getName(), "BMW");
    }

    @Test
    public void testCreateAppointment_NullStation() {
        Vehicle vehicle = new Vehicle(222L,new Brand("renoa"),new Customer("levent",25L));
        Station station = new Station(1L,null);
        Station station2 = new Station(2L, new Brand("honda"));
        Appointment appointment = new Appointment();
        appointment.setVehicle(vehicle);
        appointment.setStation(station);
        when(vehicleRepository.findByPlateCode(any(String.class))).thenReturn(Optional.of(vehicle));
        when(stationRepository.findAll()).thenReturn(List.of(station, station2));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        Appointment app = appointmentService.createAppointment("1234");
        Assertions.assertEquals("renoa",app.getVehicle().getBrand().getName());
        Assertions.assertEquals(null,app.getStation().getExclusiveBrand());
    }
    @Test
    public void testCreateAppointment_NotStation() {
        Vehicle vehicle = new Vehicle(222L,new Brand("renoa"),new Customer("levent",25L));
        Station station = new Station(1L,new Brand("bla"));
        Station station2 = new Station(2L, new Brand("honda"));
        Appointment appointment = new Appointment();
        appointment.setVehicle(vehicle);
        appointment.setStation(station);
        when(vehicleRepository.findByPlateCode(any(String.class))).thenReturn(Optional.of(vehicle));
        when(stationRepository.findAll()).thenReturn(List.of(station, station2));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> appointmentService.createAppointment("1234"));
        Assertions.assertEquals("Streamde Uygun istasyon bulunamadı!",exception.getMessage());

    }

    @Test
    public void testCreateAppointment_VehicleNotFound() {
        when(vehicleRepository.findByPlateCode("34XYZ34")).thenReturn(java.util.Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment("34XYZ34");
        });
        Assertions.assertEquals("Araç bulunamadı!", exception.getMessage());
    }

    @Test
    public void testUpdateAppointmentStatus() {

        Appointment appointment = new Appointment(11L, AppointmentStatus.PENDING, null, new Station());
        when(appointmentRepository.findById(11L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        Appointment appointment1 = appointmentService.updateAppointmentStatus(11L, "COMPLETED");
        System.out.println(appointment1.toString());
        Assertions.assertEquals(appointment1.getId(), 11L);
    }

    @Test
    public void testUpdateAppointment_NotFound() {
        when(appointmentRepository.findById(11L)).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class, () -> appointmentService.updateAppointmentStatus(11L, "CANCELED"));
    }

    @Test
    public void testUpdateAppointment_InvalidStatus() {
        Appointment appointment = new Appointment(11L, AppointmentStatus.PENDING, null, new Station());
        when(appointmentRepository.findById(11L)).thenReturn(Optional.of(appointment));
        Assertions.assertThrows(IllegalArgumentException.class, () -> appointmentService.updateAppointmentStatus(11L, "INVALID_STATUS"));
    }

    @Test
    public void testDeleteAppointment() {
        Appointment appointment = new Appointment(1L, AppointmentStatus.PENDING, new Vehicle(), new Station());
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentRepository).deleteById(1L);
        boolean b = appointmentService.deleteAppointment(1L);
        Assertions.assertTrue(b);

    }
    @Test
    public void testGetAllAppointments(){
        when(appointmentRepository.findAll()).thenReturn(List.of(new Appointment(3L)));
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        Assertions.assertEquals(3L,allAppointments.get(0).getId());
    }
    @Test
    public void testDeleteAppointment_NotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> appointmentService.deleteAppointment(1L));
        Assertions.assertEquals("Randevu bulunamadı!", exception.getMessage());
    }
}
