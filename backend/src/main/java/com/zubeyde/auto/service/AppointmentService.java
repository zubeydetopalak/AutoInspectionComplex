package com.zubeyde.auto.service;
import com.zubeyde.auto.entity.*;
import com.zubeyde.auto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Otomatik İstasyon Atama Mantığı (AVM Mantığı)
     * Markaya özel peron varsa oraya, yoksa genel perona atar.
     */
    public Appointment createAppointment(String plateCode) {
        Vehicle vehicle = vehicleRepository.findByPlateCode(plateCode)
                .orElseThrow(() -> new RuntimeException("Araç bulunamadı!"));

        // -- MANTIK: Uygun İstasyonu Bul --
        Station targetStation = findBestStationForVehicle(vehicle);
        
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setStation(targetStation);
        appointment.setVehicle(vehicle);
        int kapasite = targetStation.getCapacity();
        targetStation.setCapacity(kapasite - 1);
        stationRepository.save(targetStation);
        return appointmentRepository.save(appointment);
    }

    private Station findBestStationForVehicle(Vehicle vehicle) {
        // 1. Önce bu markaya özel bir istasyon var mı diye bak (Örn: Sadece BMW bakan yer)
        // Not: Burada basitlik adına tüm istasyonları çekip filtreliyoruz.
        List<Station> allStations = stationRepository.findAll();
        
        for (Station station : allStations) {
            // Eğer istasyonun özel bir markası varsa ve aracın markasıyla eşleşiyorsa
            if (station.getExclusiveBrand() != null && 
                station.getExclusiveBrand().getName().equals(vehicle.getBrand().getName())) {
                return station; // Özel servise yönlendir
            }
        }

        // 2. Özel istasyon yoksa, genel istasyonlardan ilk boş olanı bul
        return allStations.stream()
                .filter(s -> s.getExclusiveBrand() == null) // Genel istasyon
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Streamde Uygun istasyon bulunamadı!"));
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment updateAppointmentStatus(Long id, String status) {
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        appointment.setStatus(AppointmentStatus.valueOf(status));
        if (appointment.getStatus() == AppointmentStatus.COMPLETED|| appointment.getStatus() == AppointmentStatus.CANCELLED) {
            int guncelKapasite= appointment.getStation().getCapacity();
            Station neWstation = appointment.getStation();
            neWstation.setCapacity(guncelKapasite+1);
            stationRepository.save(neWstation);
        }
        return appointmentRepository.save(appointment);
    }

    public boolean deleteAppointment(Long id) {
        appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        appointmentRepository.deleteById(id);
        return true;
    }
}