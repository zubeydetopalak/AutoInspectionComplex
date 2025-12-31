package com.zubeyde.auto.controller;
import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    @Autowired
    private InspectionService inspectionService;

    // Muayeneyi tamamla ve sonucu hesapla
    @PostMapping("/{id}/complete")
    public Inspection completeInspection(@Valid @PathVariable(name = "id") Long id, @RequestBody List<InspectionDetail> details) {

        return inspectionService.completeInspection(id, details);
    }
    @PostMapping
    public Inspection createInspection (@Valid @RequestBody Inspection inspection) {
        return inspectionService.createInspection(inspection);
    }
    @PutMapping("/{id}")
    public Inspection updateInspection(@Valid @PathVariable(name = "id") Long id, @RequestBody Inspection inspection) {
        return inspectionService.updateInspection(id, inspection);
    }
    @GetMapping
    public List<Inspection> getAllInspections() {
        return inspectionService.getAllInspections();
    }

}