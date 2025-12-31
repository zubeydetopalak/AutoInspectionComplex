package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // Plakaya göre araç bul
    Optional<Vehicle> findByPlateCode(String plateCode);
}