package com.zubeyde.auto.service;

import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.repository.InspectionDetailRepository;
import com.zubeyde.auto.repository.InspectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionDetailService {

    @Autowired
    private InspectionDetailRepository inspectionDetailRepository;
    @Autowired
    private InspectionRepository inspectionRepository;

    public List<InspectionDetail> getInspectionDetailsByInspectionId(Long id) {
        if (inspectionRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Muayene kaydı bulunamadı!");
        }
        return inspectionDetailRepository.findByInspectionId(id);
    }

    public InspectionDetail updateInspectionDetail(Long id, InspectionDetail inspectionDetail) {
        InspectionDetail existingDetail = inspectionDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muayene detayı bulunamadı!"));

        existingDetail.setInspectorNote(inspectionDetail.getInspectorNote());
        existingDetail.setPassed(inspectionDetail.isPassed());
        existingDetail.setCheckItem(inspectionDetail.getCheckItem());

        return inspectionDetailRepository.save(existingDetail);
    }

    public InspectionDetail createInspectionDetail(InspectionDetail inspectionDetail) {
        if(inspectionRepository.findById(inspectionDetail.getInspection().getId()).isEmpty()) {
            throw new RuntimeException("Muayene kaydı bulunamadı!");
        }
        if (inspectionDetail.getCheckItem() == null) {
            throw new RuntimeException("Kontrol maddesi boş olamaz!");
        }
        return inspectionDetailRepository.save(inspectionDetail);
    }
}














