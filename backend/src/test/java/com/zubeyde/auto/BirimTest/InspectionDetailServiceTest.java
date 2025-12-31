package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.ChecklistTemplate;
import com.zubeyde.auto.entity.CriticalLevel;
import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.repository.InspectionDetailRepository;
import com.zubeyde.auto.repository.InspectionRepository;
import com.zubeyde.auto.service.InspectionDetailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class InspectionDetailServiceTest {

    @Mock
    private InspectionDetailRepository inspectionDetailRepository;
    @Mock
    private InspectionRepository inspectionRepository;
    @InjectMocks
    private InspectionDetailService inspectionDetailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void  testGetInspectionDetailsByInspectionId() {
        InspectionDetail detail=new InspectionDetail(33L,true,null);
        Inspection ins=new Inspection(1L);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(ins));
        when(inspectionDetailRepository.findByInspectionId(1L)).thenReturn(List.of(detail));
        List<InspectionDetail> inspectionDetailsByInspectionId = inspectionDetailService.getInspectionDetailsByInspectionId(1L);
        Assertions.assertEquals(1,inspectionDetailsByInspectionId.size());
    }
    @Test
    public void testGetInspectionDetailsByInspectionId_NotFound() {
        when(inspectionRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            inspectionDetailService.getInspectionDetailsByInspectionId(1L);
        });
        Assertions.assertEquals("Muayene kaydı bulunamadı!", exception.getMessage());
    }
    @Test
    public void testUpdateInspectionDetailsByInspectionId_NotFound() {
        when(inspectionRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            inspectionDetailService.updateInspectionDetail(1L,new InspectionDetail());
        });
        Assertions.assertEquals("Muayene detayı bulunamadı!", exception.getMessage());
    }
    @Test
    public void testCreateInspectionDetail_NotFoundCheckItem() {
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(new Inspection(1L)));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            inspectionDetailService.createInspectionDetail(new InspectionDetail(new Inspection(1L)));
        });
        Assertions.assertEquals("Kontrol maddesi boş olamaz!", exception.getMessage());
    }
    @Test
    public void testCreateInspectionDetail_NotFoundInspection() {
        when(inspectionRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            inspectionDetailService.createInspectionDetail(new InspectionDetail(new Inspection(1L)));
        });
        Assertions.assertEquals("Muayene kaydı bulunamadı!", exception.getMessage());
    }
    @Test
    public void testUpdateInspectionDetail(){
        InspectionDetail detail=new InspectionDetail(1L,true,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        InspectionDetail yeni=new InspectionDetail(1L,false,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        when(inspectionDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(inspectionDetailRepository.save(any(InspectionDetail.class))).thenReturn(yeni);
        InspectionDetail detail1 = inspectionDetailService.updateInspectionDetail(1L, yeni);
        Assertions.assertFalse(detail1.isPassed());
    }
    @Test
    public void testCreateInspectionDetail(){
        InspectionDetail detail=new  InspectionDetail(1L,true,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        Inspection inspection=new Inspection(22L);
        detail.setInspection(inspection);
        when(inspectionRepository.findById(any(Long.class))).thenReturn(Optional.of(inspection));
        when(inspectionDetailRepository.save(detail)).thenReturn(detail);
        InspectionDetail inspectionDetail = inspectionDetailService.createInspectionDetail(detail);
        Assertions.assertEquals(inspectionDetail.getId(),1L);
        Assertions.assertEquals(inspectionDetail.getInspection().getId(),22L);
    }


}

