package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.repository.CustomerRepository;
import com.zubeyde.auto.service.CustomerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.postgresql.hostchooser.HostRequirement.any;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testRegisterCustomer(){
        Customer customer=new Customer(1L,"Zübeyde","1234567890","5551234567");
        when(customerRepository.save(customer)).thenReturn(customer);
        Customer customer1 = customerService.registerCustomer(customer);
        assert(customer1.getId().equals(1L));
        Assertions.assertEquals(customer1.getName(),"Zübeyde");
    }
    @Test
    public void testGetCustomerByPhone(){
        Customer customer=new Customer(1L,"Zübeyde","1234567890","5551234567");
        when(customerRepository.findByPhone("5551234567")).thenReturn(Optional.of(customer));
        Customer customerByPhone = customerService.getCustomerByPhone("5551234567");
        Assertions.assertEquals(customerByPhone.getEmail(),"1234567890");
    }
    @Test
    public void testGetCustomerByPhone_NotFound(){
        when(customerRepository.findByPhone(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class,() -> customerService.getCustomerByPhone("123"));
    }
    @Test
    public void testGetVehiclesByCustomerPhone(){
        Customer customer=new Customer(1L,"Zübeyde","1234567890","555123");
        Vehicle v1=new Vehicle(1L,new Brand(),customer);
        Vehicle vehicle=new Vehicle(2L,new Brand(),customer);
        customer.setVehicles(List.of(v1,vehicle));
        when(customerRepository.findByPhone(any(String.class))).thenReturn(Optional.of(customer));
        List<Vehicle> vehiclesByCustomerPhone = customerService.getVehiclesByCustomerPhone("555123");
        Assertions.assertEquals(vehiclesByCustomerPhone.size(),2);
        Assertions.assertEquals(vehiclesByCustomerPhone.get(0).getId(),1L);

    }
    @Test
    public void testGetByCustomer_NotFound(){
        when(customerRepository.findByPhone(any(String.class))).thenReturn(Optional.empty());
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,() -> customerService.getCustomerByPhone("89"));
        Assertions.assertEquals("Müşteri bulunamadı!", thrown.getMessage());
        Assertions.assertThrows(RuntimeException.class,() -> customerService.getCustomerByPhone("89"));
    }
    @Test
    public void testGetVehiclesByCustomer_NotFound(){
        when(customerRepository.findByPhone(any(String.class))).thenReturn(Optional.empty());
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,() -> customerService.getVehiclesByCustomerPhone("89"));
        Assertions.assertEquals("Müşteri bulunamadı!", thrown.getMessage());
        Assertions.assertThrows(RuntimeException.class,() -> customerService.getCustomerByPhone("89"));
    }


    @Test
    public void testGetAllCustomers() {
        Customer customer1 = new Customer(1L, "Zübeyde", "1234567890", "555123");
        when(customerRepository.findAll()).thenReturn(List.of(customer1));
        List<Customer> allCustomers = customerService.getAllCustomers();
        Assertions.assertEquals(allCustomers.size(), 1);
        Assertions.assertEquals(allCustomers.get(0).getName(), "Zübeyde");


    }

}
