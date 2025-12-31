package com.zubeyde.auto.entity;
import jakarta.persistence.*;

@Entity
public class Vehicle {
    public Vehicle() {}

    public Vehicle(Long id,Brand brand, Customer customer) {
        this.brand = brand;
        this.customer = customer;
        this.id=id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String plateCode;
    private String modelYear;
    private String chassisNumber;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    private String vehicleType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlateCode() { return plateCode; }
    public void setPlateCode(String plateCode) { this.plateCode = plateCode; }

    public String getModelYear() { return modelYear; }
    public void setModelYear(String modelYear) { this.modelYear = modelYear; }

    public String getChassisNumber() { return chassisNumber; }
    public void setChassisNumber(String chassisNumber) { this.chassisNumber = chassisNumber; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
}