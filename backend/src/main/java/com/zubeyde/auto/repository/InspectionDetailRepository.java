package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.InspectionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionDetailRepository extends JpaRepository<InspectionDetail, Long> {
    // Bir muayeneye ait tüm detay satırlarını getir
    List<InspectionDetail> findByInspectionId(Long inspectionId);
}