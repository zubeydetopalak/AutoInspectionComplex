package com.zubeyde.auto.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubeyde.auto.controller.InspectionController;
import com.zubeyde.auto.entity.ChecklistTemplate;
import com.zubeyde.auto.entity.CriticalLevel;
import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.service.InspectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InspectionController.class)
public class InseptionIntegrationTest {

   @Autowired
   private MockMvc mockMvc;
   @Autowired
   private ObjectMapper objectMapper;

    @MockBean
    private InspectionService inspectionService;

    @Test
    public void testCompleteInspection() throws Exception {
        Inspection inspection=new Inspection(22L);
        InspectionDetail  detail=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        InspectionDetail  detail1=new InspectionDetail(new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        InspectionDetail  detail2=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        List<InspectionDetail> list=new ArrayList<>();
        list.add(detail);
        list.add(detail1);
        list.add(detail2);
       inspection.setDetails(List.of(detail,detail1,detail2));
        when(inspectionService.completeInspection(eq(22L), anyList())).thenReturn(inspection);


        mockMvc.perform(post("/api/inspections/{id}/complete",22L)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(list)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22L))
                .andExpect(jsonPath("$.details.[1].checkItem.level").value("HAFIF_KUSUR"));
    }
    @Test
    public void testCreateInspection() throws Exception {
        Inspection inspection=new Inspection(22L);
        InspectionDetail  detail=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        InspectionDetail  detail1=new InspectionDetail(new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        InspectionDetail  detail2=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        List<InspectionDetail> list=new ArrayList<>();
        list.add(detail);
        list.add(detail1);
        list.add(detail2);
        inspection.setDetails(List.of(detail,detail1,detail2));
        when(inspectionService.createInspection(any(Inspection.class))).thenReturn(inspection);


        mockMvc.perform(post("/api/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22L))
                .andExpect(jsonPath("$.details.[1].checkItem.level").value("HAFIF_KUSUR"));
    }
    @Test
    public void testUpdateInspection() throws Exception {
        Inspection inspection=new Inspection(22L);
        InspectionDetail  detail=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        InspectionDetail  detail1=new InspectionDetail(new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        InspectionDetail  detail2=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        List<InspectionDetail> list=new ArrayList<>();
        list.add(detail);
        list.add(detail1);
        list.add(detail2);
        inspection.setDetails(List.of(detail,detail1,detail2));
        when(inspectionService.updateInspection(eq(22L),any(Inspection.class))).thenReturn(inspection);


        mockMvc.perform(put("/api/inspections/{id}",22L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22L))
                .andExpect(jsonPath("$.details.[0].checkItem.level").value("KUSURSUZ"));
    }

}
