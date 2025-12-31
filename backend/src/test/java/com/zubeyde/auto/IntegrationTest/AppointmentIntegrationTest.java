package com.zubeyde.auto.IntegrationTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubeyde.auto.controller.AppointmentController;
import com.zubeyde.auto.entity.*;


import com.zubeyde.auto.service.AppointmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AppointmentController.class)
public class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AppointmentService appointmentService;

    @Test
    public void testCreateAppointment() throws Exception {
        Brand brand = new Brand("honda");
        Appointment appointment = new Appointment(1L, AppointmentStatus.PENDING,
                new Vehicle(22L, new Brand("honda"),
                        new Customer("zübeyde", 33L)),
                new Station(brand, true, 3));
        appointment.getVehicle().setPlateCode("123");
        when(appointmentService.createAppointment("123")).thenReturn(appointment);
        mockMvc.perform(post("/api/appointments")
                        .param("plate", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicle.id").value(22L))
                .andExpect(jsonPath("$.status").value("PENDING"));

    }

    @Test
    public void testGetAllAppointments() throws Exception {
        Appointment app1 = new Appointment(1L, AppointmentStatus.PENDING, new Vehicle(34L, new Brand("honda"), new Customer("levent", 34L)), new Station(new Brand("honda"), true, 3));
        Appointment app2 = new Appointment(2L, AppointmentStatus.COMPLETED, new Vehicle(33L, new Brand("bmw"), new Customer("ilyas", 33L)), new Station(new Brand("bmw"), false, 1));
        Appointment app3 = new Appointment(3L, AppointmentStatus.CANCELLED, new Vehicle(35L, new Brand("mercedes"), new Customer("samet", 35L)), new Station(new Brand("mercedes"), true, 5));
        when(appointmentService.getAllAppointments()).thenReturn(List.of(app1, app2, app3));
        mockMvc.perform(get("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(1L))
                .andExpect(jsonPath("$.[0].status").value("PENDING"))
                .andExpect(jsonPath("$.[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$.[2].vehicle.brand.name").value("mercedes"));

    }

    @Test
    public void testUpdateAppointmentStatus() throws Exception {
        Appointment app2 = new Appointment(1L, AppointmentStatus.COMPLETED, new Vehicle(34L, new Brand("honda"), new Customer("levent", 34L)), new Station(new Brand("honda"), true, 3));
        when(appointmentService.updateAppointmentStatus(1L, "COMPLETED")).thenReturn(app2);
        mockMvc.perform(put("/api/appointments/{id}", 1L)
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicle.brand.name").value("honda"));
    }
    @Test
    public void testDeleteAppointment() throws Exception {

        when(appointmentService.deleteAppointment(1L)).thenReturn(true).thenReturn(false);
         mockMvc.perform(delete("/api/appointments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        mockMvc.perform(delete("/api/appointments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));


    }

}
