package com.zubeyde.auto.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Inspection {
    @Override
    public String toString() {
        return "Inspection{" +
                "id=" + id +
                ", appointment=" + appointment +
                ", inspector=" + inspector +
                ", inspectionDate=" + inspectionDate +
                ", result='" + result + '\'' +
                ", notes='" + notes + '\'' +
                ", details=" + details +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Inspection(Long id) {
        this.id = id;
    }
    public Inspection(){}

    public Inspection(Long id, Appointment appointment) {
        this.id = id;
        this.appointment = appointment;
    }

    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee inspector;

    private LocalDateTime inspectionDate;
    
    private String result;
    private String notes;
    
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL)
    private List<InspectionDetail> details;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Employee getInspector() { return inspector; }
    public void setInspector(Employee inspector) { this.inspector = inspector; }

    public LocalDateTime getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDateTime inspectionDate) { this.inspectionDate = inspectionDate; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<InspectionDetail> getDetails() { return details; }
    public void setDetails(List<InspectionDetail> details) { this.details = details; }
}