package com.zubeyde.auto.service;
import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.repository.BrandRepository;
import com.zubeyde.auto.repository.CustomerRepository;
import com.zubeyde.auto.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BrandRepository brandRepository;
    public Vehicle registerVehicle(Vehicle vehicle) {
        // Plaka kontrolü yapılabilir
        Optional<Vehicle> existing = vehicleRepository.findByPlateCode(vehicle.getPlateCode());
        System.out.println(existing.toString());
        if (existing.isPresent()) {
            throw new RuntimeException("Bu plaka zaten kayıtlı!");
        }
        Customer owner = vehicle.getCustomer();
        if (customerRepository.findById(owner.getId()).isEmpty()) {
            throw new RuntimeException("Araç sahibi kayıtlı değil!");

        }
        Brand brand = vehicle.getBrand();
        Brand existingBrand = brandRepository.findByName(brand.getName())
                .orElseThrow(() -> new RuntimeException("Araç markası kayıtlı değil!"));
        
        
    
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}