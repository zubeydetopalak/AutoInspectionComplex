package com.zubeyde.auto.service;
import com.zubeyde.auto.entity.Station;
import com.zubeyde.auto.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    public Station addStation(Station station) {
        return stationRepository.save(station);
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public List<Station> getStationsBrandIsNull(){
        return stationRepository.findByExclusiveBrandIsNull();
    };

    public Station updateStation(Station station) {
        Station existingStation = stationRepository.findById(station.getId())
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));

        existingStation.setExclusiveBrand(station.getExclusiveBrand());
        existingStation.setStationCode(station.getStationCode());
        existingStation.setOpen(station.isOpen());
        existingStation.setCapacity(station.getCapacity());

        return stationRepository.save(existingStation);
    }
}