package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    // Sonuca göre listele (Örn: "KALDI" olanları getir)
    List<Inspection> findByResult(String result);
    
    // Belirli bir ustanın yaptığı muayeneler
    List<Inspection> findByInspectorId(Long inspectorId);
}