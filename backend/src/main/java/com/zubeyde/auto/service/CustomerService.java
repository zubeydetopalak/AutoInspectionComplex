package com.zubeyde.auto.service;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer registerCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı!"));
    }

    public List<Vehicle> getVehiclesByCustomerPhone(String phone) {
        
        Customer customer = getCustomerByPhone(phone);
        // if(customer.getVehicles().isEmpty()) {
        //     throw new RuntimeException("Bu müşteriye ait araç bulunamadı!");
        // }
        return customer.getVehicles();
    }
}