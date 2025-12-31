package com.zubeyde.auto.IntegrationTest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubeyde.auto.controller.VehicleController;

import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.service.VehicleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.naming.PartialResultException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(VehicleController.class)
public class VehicleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private VehicleService vehicleService;

    @Test
    public void testRegisterVehicle() throws Exception {
        Vehicle vehicle=new Vehicle(22L,new Brand("bmw"),new Customer("levent",99L));
        when(vehicleService.registerVehicle(any(Vehicle.class))).thenReturn(vehicle);
        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(vehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22L))
                .andExpect(jsonPath("$.customer.name").value("levent"));
    }
    @Test
    public void testgetAllVehicles() throws Exception {
        Vehicle vehicle=new Vehicle(22L,new Brand("bmw"),new Customer("levent",99L));
        Vehicle vehicle1=new Vehicle(23L,new Brand("mustang"),new Customer("zübeyde",100L));
        Vehicle vehicle2=new Vehicle(24L,new Brand("hacı murat"),new Customer("rüm",89L));
        when(vehicleService.getAllVehicles()).thenReturn(List.of(vehicle,vehicle1,vehicle2));
        mockMvc.perform(get("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(22L))
                .andExpect(jsonPath("$.[1].brand.name").value("mustang"))
                .andExpect(jsonPath("$", hasSize(3)));

    }
}
