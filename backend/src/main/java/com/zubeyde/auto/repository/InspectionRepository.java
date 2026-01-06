package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByResult(String result);

    List<Inspection> findByInspectorId(Long inspectorId);
}