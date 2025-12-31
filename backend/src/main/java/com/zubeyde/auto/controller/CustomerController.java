package com.zubeyde.auto.controller;

import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public Customer registerCustomer(@Valid @RequestBody Customer customer) {
        return customerService.registerCustomer(customer);
    }
    @GetMapping("/getAllCustomers")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }
   @GetMapping("/getByPhone/{phone}")
   public Customer getCustomerByPhone(@Valid @PathVariable(name = "phone") String phone) {
         return customerService.getCustomerByPhone(phone);
   }
   @GetMapping("/getVehicles/{phone}")
    public List<Vehicle> getVehiclesByCustomerPhone(@Valid @PathVariable(name = "phone") String phone) {
        return customerService.getVehiclesByCustomerPhone(phone);
}}
