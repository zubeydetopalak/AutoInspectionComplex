package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.repository.BrandRepository;
import com.zubeyde.auto.repository.CustomerRepository;
import com.zubeyde.auto.repository.VehicleRepository;
import com.zubeyde.auto.service.VehicleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class VehicleServiceTest {


    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testRegisterVehicle(){
        Vehicle vehicle=new Vehicle(1L,new Brand("bmw"),new Customer("Zübeyde",11L));
        vehicle.setPlateCode("123");
        when(vehicleRepository.findByPlateCode("123")).thenReturn(Optional.empty());
        when(customerRepository.findById(11L)).thenReturn(Optional.of(new Customer("Zübeyde",11L)));
        when(brandRepository.findByName("bmw")).thenReturn(Optional.of(new Brand("bmw")));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);
        Vehicle veh = vehicleService.registerVehicle(vehicle);
        Assertions.assertEquals(1L,veh.getId());
        Assertions.assertEquals("Zübeyde",veh.getCustomer().getName());
    }
    @Test
    public void testRegisterVehicle_AlreadyExist(){
        Vehicle vehicle=new Vehicle(1L,new Brand("bmw"),new Customer("Zübeyde",11L));
        vehicle.setPlateCode("123");
        when(vehicleRepository.findByPlateCode("123")).thenReturn(Optional.of(new Vehicle()));
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> vehicleService.registerVehicle(vehicle));
        Assertions.assertEquals("Bu plaka zaten kayıtlı!",exception.getMessage());
    }
    @Test
    public void testRegisterVehicle_CustomerNotExist(){
        Vehicle vehicle=new Vehicle(1L,new Brand("bmw"),new Customer("Zübeyde",11L));
        vehicle.setPlateCode("123");
        when(customerRepository.findById(11L)).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> vehicleService.registerVehicle(vehicle));
        Assertions.assertEquals("Araç sahibi kayıtlı değil!",exception.getMessage());
    }
    @Test
    public void testRegisterVehicle_BrandNotExist(){
        Vehicle vehicle=new Vehicle(1L,new Brand("bmw"),new Customer("Zübeyde",11L));
        vehicle.setPlateCode("123");
        when(customerRepository.findById(11L)).thenReturn(Optional.of(new Customer("Zübeyde",11L)));
        when(brandRepository.findByName("bmw")).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> vehicleService.registerVehicle(vehicle));
        Assertions.assertEquals("Araç markası kayıtlı değil!",exception.getMessage());
    }
    @Test
    public void testGetAllVehicles(){
        Vehicle v1=new Vehicle(1L,new Brand(),new Customer());
        Vehicle vehicle=new Vehicle(2L,new Brand(),new Customer());
        when(vehicleRepository.findAll()).thenReturn(java.util.List.of(v1,vehicle));
        java.util.List<Vehicle> allVehicles = vehicleService.getAllVehicles();
        Assertions.assertEquals(2,allVehicles.size());
        Assertions.assertEquals(1L,allVehicles.get(0).getId());
    }


}
