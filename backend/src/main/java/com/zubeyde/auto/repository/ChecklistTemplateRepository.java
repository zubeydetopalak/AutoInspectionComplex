package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {
    // Araç tipine göre soruları getir (Örn: "Binek" araç soruları)
    List<ChecklistTemplate> findByVehicleType(String vehicleType);
}