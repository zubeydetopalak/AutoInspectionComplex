package com.zubeyde.auto.IntegrationTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubeyde.auto.controller.CustomerController;
import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CustomerController.class)
public class CustomerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    public void testRegisterCustomer() throws Exception {
        Customer customer = new Customer("zübeyde", 1L);
        when(customerService.registerCustomer(any(Customer.class))).thenReturn(customer);
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("zübeyde"))
                .andExpect(jsonPath("$.id").value(1L));

    }

    @Test
    public void testGetAllCustomers() throws Exception {
        Customer customer = new Customer("zübeyde", 1L);
        Customer customer1 = new Customer("menesse", 2L);
        Customer customer2 = new Customer("parla", 3L);
        when(customerService.getAllCustomers()).thenReturn(List.of(customer1, customer2, customer));
        mockMvc.perform(get("/api/customers/getAllCustomers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("menesse"))
                .andExpect(jsonPath("$.[0].id").value(2L))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testGetCustomerByPhone() throws Exception {
        Customer customer = new Customer(1L, "Zübeyde", "tdjlsjfl", "5398945909");
        when(customerService.getCustomerByPhone("5398945909")).thenReturn(customer);
        mockMvc.perform(get("/api/customers/getByPhone/{phone}", "5398945909")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zübeyde"))
                .andExpect(jsonPath("$.phone").value("5398945909"));

    }

    @Test
    public void testGetVehiclesByCustomerPhone() throws Exception {

        Customer customer = new Customer(1L, "Zübeyde", "tdjlsjfl", "5398945909");
        Vehicle v = new Vehicle(2L, new Brand("honda"), customer);
        Vehicle v2 = new Vehicle(3L, new Brand("mercedes"), customer);
        Vehicle v3 = new Vehicle(4L, new Brand("mercedes"), customer);
        List<Vehicle> list = new ArrayList<>();
        list.add(v);
        list.add(v2);
        list.add(v3);
        customer.setVehicles(list);

        when(customerService.getVehiclesByCustomerPhone("5398945909")).thenReturn(customer.getVehicles());
        mockMvc.perform(get("/api/customers/getVehicles/{phone}", "5398945909")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.[0].brand.name").value("honda"))
                        .andExpect(jsonPath("$.[1].customer.phone").value("5398945909"))
                .andExpect(jsonPath("$.[2].customer.name").value("Zübeyde"));


    }
}
