package com.zubeyde.auto.controller;

import com.zubeyde.auto.entity.Station;
import com.zubeyde.auto.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @PostMapping
    public Station createStation(@RequestBody Station station) {
        return stationService.addStation(station);
    }

    @PutMapping
    public Station updateStation(@RequestBody Station station) {
        return stationService.updateStation(station);
    }

}
