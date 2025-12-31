package com.zubeyde.auto.IntegrationTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubeyde.auto.controller.StationController;
import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Station;
import com.zubeyde.auto.service.StationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// Bunu eklersen content(), status(), jsonPath() hepsi gelir:
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StationController.class)
public class StationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private StationService stationService;

    @Test
    public void testgetAllStations() throws Exception {
        Station station = new Station(new Brand("honda"), true, 7);
        Station station1 = new Station(new Brand("mustang"), false, 2);
        Station station2 = new Station(new Brand("ferrari"), false, 4);
        Station station3 = new Station(new Brand("Ford"), true, 1);
        when(stationService.getAllStations()).thenReturn(List.of(station, station1, station2, station3));
        mockMvc.perform(get("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].open").value("true"))
                .andExpect(jsonPath("$.[1].exclusiveBrand.name").value("mustang"))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void testUpdateStations() throws Exception {
        Station station = new Station(new Brand("honda"), true, 7);
        Station station1 = new Station(new Brand("mustang"), false, 2);
        when(stationService.updateStation(any(Station.class))).thenReturn(station1);
        mockMvc.perform(put("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(station)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value("false"))
                .andExpect(jsonPath("$.exclusiveBrand.name").value("mustang"))
        ;
    }

    @Test

    public void testUpdateStation_WhenStationNotFound_ShouldReturnProblemDetail() throws Exception {

        Station stationInput = new Station(new Brand("honda"), true, 5);
        stationInput.setId(99L);


        String expectedMessage = "İstasyon bulunamadı!";
        when(stationService.updateStation(any(Station.class)))
                .thenThrow(new RuntimeException(expectedMessage));
        mockMvc.perform(put("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stationInput)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))

                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").exists());
    }

}
