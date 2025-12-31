package com.zubeyde.auto.entity;
import jakarta.persistence.*;

@Entity
public class Brand {
    public Brand() {}

    public Brand(String name) {
        this.name = name;
    }

    public Brand(Long id, String name, String originCountry) {
        this.id = id;
        this.name = name;
        this.originCountry = originCountry;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; 

    @Column(name = "origin_country")
    private String originCountry;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }
}