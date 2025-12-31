package com.zubeyde.auto.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    public Appointment() {}

    public Appointment(Long id) {
        this.id = id;
    }

    public Appointment(Long id, AppointmentStatus status, Vehicle vehicle, Station station) {
        this.id = id;

        this.status = status;
        this.vehicle = vehicle;
        this.station = station;
    }

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Station getStation() { return station; }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", status=" + status +
                ", vehicle=" + vehicle +
                ", station=" + station +
                '}';
    }

    public void setStation(Station station) { this.station = station; }
}

