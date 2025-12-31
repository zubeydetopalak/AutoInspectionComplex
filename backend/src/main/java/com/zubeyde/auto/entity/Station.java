package com.zubeyde.auto.entity;
import jakarta.persistence.*;

@Entity
public class Station {
    public Station() {}

    public Station(Long id,Brand exclusiveBrand) {
        this.exclusiveBrand = exclusiveBrand;
        this.id=id;
    }

    public Station(Brand brand,boolean isOpen, int capacity) {
        this.isOpen = isOpen;
        this.capacity = capacity;
        this.exclusiveBrand=brand;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", unique = true)
    private String stationCode;

    @ManyToOne
    @JoinColumn(name = "exclusive_brand_id")
    private Brand exclusiveBrand; 

    private boolean isOpen; 
    private int capacity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }
    
    public Brand getExclusiveBrand() { return exclusiveBrand; }
    public void setExclusiveBrand(Brand exclusiveBrand) { this.exclusiveBrand = exclusiveBrand; }
    
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}