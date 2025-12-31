package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Appointment;
import com.zubeyde.auto.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Belirli durumdaki randevuları getir (Örn: PENDING olanlar)
    List<Appointment> findByStatus(AppointmentStatus status);
    

}