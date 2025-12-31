package com.zubeyde.auto.IntegrationTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubeyde.auto.controller.InspectionDetailController;
import com.zubeyde.auto.entity.ChecklistTemplate;
import com.zubeyde.auto.entity.CriticalLevel;
import com.zubeyde.auto.entity.Inspection;
import com.zubeyde.auto.entity.InspectionDetail;
import com.zubeyde.auto.service.InspectionDetailService;
import com.zubeyde.auto.service.InspectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.ArrayList;
import java.util.List;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InspectionDetailController.class)
public class InspectionDetailsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InspectionDetailService inspectionDetailService;

@Test
    public  void testgetAllInspectionDetails() throws Exception {
    InspectionDetail  detail=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
    InspectionDetail  detail1=new InspectionDetail(new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
    InspectionDetail  detail2=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
    List<InspectionDetail> list=new ArrayList<>();
    list.add(detail);
    list.add(detail1);
    list.add(detail2);
    when(inspectionDetailService.getInspectionDetailsByInspectionId(22L)).thenReturn(list);
    mockMvc.perform(get("/api/inspection-details/{id}",22L)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].checkItem.level").value("KUSURSUZ"));
}
    @Test
    public  void testUpdateInspectionDetails() throws Exception {
        InspectionDetail  detail=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        InspectionDetail  detail1=new InspectionDetail(new ChecklistTemplate(CriticalLevel.HAFIF_KUSUR));
        InspectionDetail  detail2=new InspectionDetail(new ChecklistTemplate(CriticalLevel.KUSURSUZ));
        List<InspectionDetail> list=new ArrayList<>();
        list.add(detail);
        list.add(detail1);
        list.add(detail2);
        when(inspectionDetailService.updateInspectionDetail(eq(22L),any(InspectionDetail.class))).thenReturn(detail);
        mockMvc.perform(put("/api/inspection-details/{id}",22L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detail1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkItem.level").value("KUSURSUZ"));
    }
    @Test
    public void testCreateInspectionDetail() throws Exception {
    Inspection inspection=new Inspection(22L);
    InspectionDetail detail=new InspectionDetail(1L,true,new ChecklistTemplate(CriticalLevel.AGIR_KUSUR));
    detail.setInspection(inspection);
    when(inspectionDetailService.createInspectionDetail(any(InspectionDetail.class))).thenReturn(detail);



    mockMvc.perform(post("/api/inspection-details")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(detail)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.passed").value("true"));
    }


}
