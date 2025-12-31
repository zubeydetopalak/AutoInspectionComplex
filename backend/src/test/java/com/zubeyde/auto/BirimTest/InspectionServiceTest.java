package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.*;
import com.zubeyde.auto.exception.NullException;
import com.zubeyde.auto.repository.AppointmentRepository;
import com.zubeyde.auto.repository.EmployeeRepository;
import com.zubeyde.auto.repository.InspectionDetailRepository;
import com.zubeyde.auto.repository.InspectionRepository;
import com.zubeyde.auto.service.InspectionService;
import com.zubeyde.auto.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

public class InspectionServiceTest {
    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionDetailRepository detailRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private NotificationService notification;
    @InjectMocks
    private InspectionService inspectionService;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateInspection(){
        Appointment app= new Appointment(1L, AppointmentStatus.PENDING,null,null);
        Inspection inspection=new Inspection(11L);
        inspection.setAppointment(app);
        when(appointmentRepository.findById(any(Long.class))).thenReturn(Optional.of(app));
        when(inspectionRepository.save(inspection)).thenReturn(inspection);
        Inspection  ins = inspectionService.createInspection(inspection);
        Assertions.assertEquals(ins.getId(),11L);
        Assertions.assertEquals(ins.getAppointment().getId(),1L);
    }
    @Test
    public void testUpdateInspection(){
        Inspection inspection=new Inspection(11L);
        Inspection inspection1=new Inspection(13L);
        when(inspectionRepository.findById(11L)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.save(inspection)).thenReturn(inspection1);
        Inspection newIns = inspectionService.updateInspection(11L, inspection1);
        Assertions.assertEquals(newIns.getId(),13L);

    }
    @Test
    public void testUpdateInspection_NotFound(){

        Inspection inspection=new Inspection(11L);
        when(inspectionRepository.findById(11L)).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            inspectionService.updateInspection(11L, inspection);
        });
        Assertions.assertEquals("Muayene bulunamadı: 11", exception.getMessage());
    }
    @Test
    public void testCompleteInspesction_NotPass(){
        Inspection inspection=new Inspection(11L);
        List<InspectionDetail> list=new ArrayList<>();
        InspectionDetail first=new InspectionDetail(1L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(first);
        InspectionDetail second= new InspectionDetail(2L,false,new ChecklistTemplate(CriticalLevel.AGIR_KUSUR));
        list.add(second);
        inspection.setDetails(list);
        when(inspectionRepository.findById(11L)).thenReturn(Optional.of(inspection));
        when(detailRepository.save(first)).thenReturn(first);
        when(detailRepository.save(second)).thenReturn(second);
        when(inspectionRepository.save(inspection)).thenReturn(inspection);
        when(notification.IncspectionCompletedNotification(any(Inspection.class))).thenReturn("tamam");
        Inspection inspection1 = inspectionService.completeInspection(11L, list);
        Assertions.assertEquals(inspection1.getResult(),"KALDI");

        Assertions.assertEquals(inspection1.getDetails().size(),2);
        Assertions.assertEquals(inspection1.getId(),11L);
        verify(notification, times(1)).IncspectionCompletedNotification(any());

    }
    @Test
    public void testCompleteInspesction_Pass() {
        Inspection inspection = new Inspection(11L);
        List<InspectionDetail> list = new ArrayList<>();
        InspectionDetail first = new InspectionDetail(1L, false, new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(first);
        InspectionDetail second = new InspectionDetail(2L, false, new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(second);
        inspection.setDetails(list);
        when(inspectionRepository.findById(11L)).thenReturn(Optional.of(inspection));
        when(detailRepository.save(first)).thenReturn(first);
        when(detailRepository.save(second)).thenReturn(second);
        when(inspectionRepository.save(inspection)).thenReturn(inspection);
        Inspection inspection1 = inspectionService.completeInspection(11L, list);
        Assertions.assertEquals(inspection1.getResult(), "GEÇTİ");

        Assertions.assertEquals(inspection1.getDetails().size(), 2);
        Assertions.assertEquals(inspection1.getId(), 11L);
    }
        @Test
        public void testCompleteInspesction_PassPerfect(){
            List<InspectionDetail> list=new ArrayList<>();
            Inspection inspection=new Inspection(11L);

            InspectionDetail first=new InspectionDetail(1L,false,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
            list.add(first);
            InspectionDetail second= new InspectionDetail(2L,false,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
            list.add(second);
            inspection.setDetails(list);
            when(inspectionRepository.findById(11L)).thenReturn(Optional.of(inspection));
            when(detailRepository.save(first)).thenReturn(first);
            when(detailRepository.save(second)).thenReturn(second);
            when(inspectionRepository.save(inspection)).thenReturn(inspection);
            Inspection inspection1 = inspectionService.completeInspection(11L, list);
            Assertions.assertEquals(inspection1.getResult(),"GEÇTİ");

            Assertions.assertEquals(inspection1.getDetails().size(),2);
            Assertions.assertEquals(inspection1.getId(),11L);

    }
    @Test
    public void testCompleteInspesction_ConditionPass(){
        Inspection inspection=new Inspection(11L);
        List<InspectionDetail> list=new ArrayList<>();
        InspectionDetail first=new InspectionDetail(1L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(first);
        InspectionDetail second= new InspectionDetail(2L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(second);
        InspectionDetail third= new InspectionDetail(3L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(third);
        InspectionDetail forth= new InspectionDetail(3L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        list.add(forth);
        inspection.setDetails(list);
        when(inspectionRepository.findById(11L)).thenReturn(Optional.of(inspection));

        when(inspectionRepository.save(any(Inspection.class))).thenReturn(inspection);
        Inspection inspection1 = inspectionService.completeInspection(11L, list);
        Assertions.assertEquals(inspection1.getResult(),"ŞARTLI GEÇTİ");

        Assertions.assertEquals(4,inspection1.getDetails().size());
        Assertions.assertEquals(inspection1.getId(),11L);

    }
    @Test
    public void testCompleteInspection_NotFound(){
        when(inspectionRepository.findById(anyLong())).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> inspectionService.completeInspection(11L, List.of()));
        Assertions.assertEquals("Muayene bulunamadı: 11",exception.getMessage());
    }
    @Test
    public void testCompleteInspection_NotFoundDetails(){
        Inspection inspection=new Inspection(11L);
        when(inspectionRepository.findById(anyLong())).thenReturn(Optional.of(inspection));
        RuntimeException exception = Assertions.assertThrows(NullException.class, () -> inspectionService.completeInspection(11L, List.of()));
        Assertions.assertEquals("İnceleme detayı boş",exception.getMessage());
    }
    @Test
    public void testCreateInspection_NotFound(){
        Inspection inspection=new Inspection(11L,new Appointment(22L));
        when(inspectionRepository.findById(11L)).thenReturn(Optional.empty());
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> inspectionService.createInspection(inspection));
        Assertions.assertEquals("Randevu bulunamadı: 22",exception.getMessage());
    }
    @Test
    public void testCompleteInspectionIfDetails_Pass(){
        Inspection inspection=new Inspection(33L);
        System.out.println(inspection.toString());

        InspectionDetail detail=new InspectionDetail(22L,false,null);
        inspection.setDetails(List.of(detail));
        System.out.println(inspection.toString());
        when(inspectionRepository.findById(any(Long.class))).thenReturn(Optional.of(inspection));
        when(inspectionRepository.save(any(Inspection .class))).thenReturn(inspection);
        Inspection insp = inspectionService.completeInspection(33L, List.of(detail));
        Assertions.assertEquals(insp.getDetails().get(0).getId(),22L);
        System.out.println(insp.toString());


    }
    @Test
    public void testCompleteInspection_PassOrFail(){
        Inspection inspection=new Inspection(33L);
        InspectionDetail detail=new InspectionDetail(22L,true,null);
        InspectionDetail detail1=new InspectionDetail(23L,false,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        InspectionDetail detail2=new InspectionDetail(24L,false,new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        inspection.setDetails(List.of(detail,detail1,detail2));
        when(inspectionRepository.findById(any(Long.class))).thenReturn(Optional.of(inspection));
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(inspection);
        Inspection inspection1 = inspectionService.completeInspection(11L, List.of(detail, detail1, detail2));
        Assertions.assertEquals("GEÇTİ",inspection1.getResult());
    }
    @Test
    public void testCompleteInspection_Pass(){
        Inspection inspection=new Inspection(33L);
        InspectionDetail detail=new InspectionDetail(22L,true,null);
        InspectionDetail detail1=new InspectionDetail(23L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        InspectionDetail detail2=new InspectionDetail(24L,false,new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        inspection.setDetails(List.of(detail,detail1,detail2));
        when(inspectionRepository.findById(any(Long.class))).thenReturn(Optional.of(inspection));
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(inspection);
        Inspection inspection1 = inspectionService.completeInspection(11L, List.of(detail, detail1, detail2));
        Assertions.assertEquals("GEÇTİ",inspection1.getResult());
    }
    @Test
    public void testCompleteInspection_Pafdfdss(){
        Inspection inspection=new Inspection(33L);
        InspectionDetail detail=new InspectionDetail(22L,true,null);
        InspectionDetail detail1=new InspectionDetail(23L,false,new ChecklistTemplate());
        InspectionDetail detail2=new InspectionDetail(24L,false,new ChecklistTemplate());
        inspection.setDetails(List.of(detail,detail1,detail2));
        when(inspectionRepository.findById(any(Long.class))).thenReturn(Optional.of(inspection));
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(inspection);
        Inspection inspection1 = inspectionService.completeInspection(11L, List.of(detail, detail1, detail2));
        Assertions.assertEquals(33L,inspection1.getId());
    }



}














