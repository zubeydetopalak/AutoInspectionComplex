package com.zubeyde.auto.entity;
import jakarta.persistence.*;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String role; 

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }
}