package com.zubeyde.auto.entity;
import jakarta.persistence.*;

@Entity
public class ChecklistTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public ChecklistTemplate() {
    }

    public ChecklistTemplate(CriticalLevel level) {
        this.level = level;
    }

    private String description;
    private String category;
    
    @Enumerated(EnumType.STRING)
    private CriticalLevel level; 
    
    private String vehicleType; 

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public CriticalLevel getLevel() { return level; }
    public void setLevel(CriticalLevel level) { this.level = level; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
}

