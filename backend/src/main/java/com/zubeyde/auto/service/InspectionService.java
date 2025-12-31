package com.zubeyde.auto.service;
import com.zubeyde.auto.entity.*;
import com.zubeyde.auto.exception.NullException;
import com.zubeyde.auto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InspectionService {

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private InspectionDetailRepository detailRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ChecklistTemplateRepository checklistTemplateRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    
    public Inspection completeInspection(Long inspectionId, List<InspectionDetail> details)  {
        
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Muayene bulunamadı: " + inspectionId));

        boolean agirKusurVar = false;
        int hafifKusurSayisi = 0;
        if (details.isEmpty()){
            throw new NullException("İnceleme detayı boş");
        }

        for (InspectionDetail detail : details) {

            detail.setInspection(inspection);
            
            if (detail.getCheckItem() != null && detail.getCheckItem().getId() != null) {
                ChecklistTemplate template = checklistTemplateRepository.findById(detail.getCheckItem().getId()).orElse(null);
                detail.setCheckItem(template);
            }

            detailRepository.save(detail);


            if (!detail.isPassed()) {
                ChecklistTemplate kural = detail.getCheckItem();
                
                if (kural != null) {
                    if (kural.getLevel() == CriticalLevel.AGIR_KUSUR) {
                        agirKusurVar = true;
                    } else if (kural.getLevel() == CriticalLevel.HAFIF_KUSUR) {
                        hafifKusurSayisi++;
                    }
                }
            }
        }

        // KARAR MEKANİZMASI
        String sonuc="KALDI";
        if (agirKusurVar) {
            sonuc = "KALDI"; // Tek bir ağır kusur bile bırakır
        } else if (hafifKusurSayisi > 3) {
            sonuc = "ŞARTLI GEÇTİ"; // 3 Hafif kusur şartlı geçirir
        }else {
            sonuc = "GEÇTİ"; // Temiz
        }
        
        inspection.setResult(sonuc);
        inspection.setInspectionDate(LocalDateTime.now());
        
        if (inspection.getAppointment() != null) {
             Appointment appointment = inspection.getAppointment();
                appointment.setStatus(AppointmentStatus.COMPLETED);
                appointmentRepository.save(appointment);
        
        }

        notificationService.IncspectionCompletedNotification(inspection);
        return inspectionRepository.save(inspection);
    }


    public Inspection createInspection(Inspection inspection) {
        if(appointmentRepository.findById(inspection.getAppointment().getId()).isEmpty()) {
            throw new RuntimeException("Randevu bulunamadı: " + inspection.getAppointment().getId());
        }
        
        // Use existing Inspector or create one if not exists
        Employee inspector = employeeRepository.findByName("Inspector")
                .orElseGet(() -> {
                    Employee newInspector = new Employee();
                    newInspector.setName("Inspector");
                    newInspector.setRole("INSPECTOR");
                    // Assign to the station of the appointment if possible
                    Appointment app = appointmentRepository.findById(inspection.getAppointment().getId()).orElse(null);
                    if (app != null && app.getStation() != null) {
                        newInspector.setStation(app.getStation());
                    }
                    return employeeRepository.save(newInspector);
                });
        
        inspection.setInspector(inspector);
        
        return inspectionRepository.save(inspection);
    }

    public Inspection updateInspection(Long id, Inspection inspection) {
        Inspection existingInspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muayene bulunamadı: " + id));

        existingInspection.setResult(inspection.getResult());
        existingInspection.setNotes(inspection.getNotes());
        existingInspection.setInspectionDate(inspection.getInspectionDate());

        if (inspection.getDetails() != null) {
            List<InspectionDetail> incomingDetails = inspection.getDetails();
            List<InspectionDetail> existingDetails = existingInspection.getDetails();

            Set<Long> incomingIds = incomingDetails.stream()
                    .map(InspectionDetail::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (existingDetails != null) {
                List<InspectionDetail> toDelete = existingDetails.stream()
                        .filter(d -> !incomingIds.contains(d.getId()))
                        .collect(Collectors.toList());
                detailRepository.deleteAll(toDelete);
            }

            for (InspectionDetail detail : incomingDetails) {
                detail.setInspection(existingInspection);
                if (detail.getCheckItem() != null && detail.getCheckItem().getId() != null) {
                    ChecklistTemplate template = checklistTemplateRepository.findById(detail.getCheckItem().getId()).orElse(null);
                    detail.setCheckItem(template);
                }
                detailRepository.save(detail);
            }
        }

        return inspectionRepository.save(existingInspection);
    }

    public List<Inspection> getAllInspections() {
        return inspectionRepository.findAll();
    }
}