package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByStationId(Long stationId);
    @Query("SELECT e FROM Employee e WHERE e.name = ?1")
    public Optional<Employee> findByName(String name);
}