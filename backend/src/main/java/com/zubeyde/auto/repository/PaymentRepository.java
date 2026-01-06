package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByInspectionId(Long inspectionId);
}