package com.zubeyde.auto.controller;
import com.zubeyde.auto.entity.Appointment;
import com.zubeyde.auto.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Örnek istek: POST /api/appointments?plate=34ABC123&time=2023-12-01T10:00:00
    @PostMapping
    public Appointment createAppointment(@RequestParam String plate) {
        return appointmentService.createAppointment(plate);
    }
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }
    @PutMapping("/{id}")
    public Appointment updateAppointmentStatus(@Valid  @PathVariable Long id, @RequestParam String status) {
        return appointmentService.updateAppointmentStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public boolean deleteAppointment(@Valid  @PathVariable Long id) {
            return appointmentService.deleteAppointment(id);
    }
}