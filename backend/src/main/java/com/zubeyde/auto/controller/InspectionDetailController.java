package com.zubeyde.auto.controller;


import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.service.InspectionDetailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspection-details")
public class InspectionDetailController {

    @Autowired
    private InspectionDetailService inspectionDetailService;

    @GetMapping("/{id}")
    public List<InspectionDetail> getAllInspectionDetails(@Valid @PathVariable(name = "id") Long id) {

        return inspectionDetailService.getInspectionDetailsByInspectionId(id);
    }
    @PutMapping("/{id}")
    public InspectionDetail updateInspectionDetail(@Valid @PathVariable(name = "id") Long id, @RequestBody InspectionDetail inspectionDetail) {

        return inspectionDetailService.updateInspectionDetail(id,inspectionDetail);
    }
    @PostMapping
    public InspectionDetail createInspectionDetail (@Valid @RequestBody InspectionDetail inspectionDetail) {
        return inspectionDetailService.createInspectionDetail(inspectionDetail);
    }
}
