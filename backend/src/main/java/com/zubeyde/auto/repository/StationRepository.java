package com.zubeyde.auto.repository;
import com.zubeyde.auto.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    // İstasyon koduna göre bul (Örn: "PERON-A1")
    Station findByStationCode(String stationCode);

    List<Station> findByExclusiveBrandIsNull();
}