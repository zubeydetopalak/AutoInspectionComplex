package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Telefona göre müşteri bul
    Optional<Customer> findByPhone(String phone);
    
    // Email'e göre müşteri bul
    Optional<Customer> findByEmail(String email);


}