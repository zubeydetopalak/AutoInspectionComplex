package com.zubeyde.auto.controller;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @PostMapping
    public Vehicle registerVehicle(@Valid @RequestBody Vehicle vehicle) {
        return vehicleService.registerVehicle(vehicle);
    }

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }
}