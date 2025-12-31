package com.zubeyde.auto.BirimTest;

import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.Station;
import com.zubeyde.auto.repository.StationRepository;
import com.zubeyde.auto.service.StationService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class StationServiceTest {

    @Mock
    private StationRepository stationRepository;
    @InjectMocks
    private StationService stationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddStation() {
        Station station = new Station(1L, new Brand(2L, "BMW", "Almanya"));
        when(stationRepository.save(station)).thenReturn(station);
        Station station1 = stationService.addStation(station);
        Assertions.assertEquals(station1.getExclusiveBrand().getName(), "BMW");
    }


    @Test
    public void testGetAllStation() {
        Station first = new Station(1L, new Brand(1L, "BMW", "Almanya"));
        Station second = new Station(2L, new Brand(2L, "MERCEDES", "Almanya"));
        Station third = new Station(3L, new Brand(3L, "HONDA", "JAPONYA"));
        Station station = new Station(4L, new Brand(4L, "MUSTANG", "ABD"));
        when(stationRepository.findAll()).thenReturn(List.of(first, second, third, station));
        List<Station> list = stationService.getAllStations();
        Assertions.assertEquals(4, list.size());
        Assertions.assertEquals("HONDA", list.get(2).getExclusiveBrand().getName());
    }

    @Test
    public void testGetStationBrandIsNull() {
        Station station = new Station(1L, null);
        when(stationRepository.findByExclusiveBrandIsNull()).thenReturn(List.of(station));
        List<Station> stationsBrandIsNull = stationService.getStationsBrandIsNull();
        Assertions.assertNull(stationsBrandIsNull.get(0).getExclusiveBrand());
        Assertions.assertEquals(1, stationsBrandIsNull.size());
    }

    @Test
    public void testUpdateStation(){
       Station station=new Station(new Brand("BMW"),true,1);
       station.setId(1L);
        Station stationUpdated=new Station(new Brand("BMW"),false,0);
       when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
       when(stationRepository.save(station)).thenReturn(stationUpdated);
        Station yeni = stationService.updateStation(station);
        Assertions.assertEquals(false,yeni.isOpen());
        Assertions.assertEquals(0,yeni.getCapacity());
    }
    @Test
    public void setStationRepository(){
        Station station=new Station(new Brand("BMW"),true,1);
        station.setId(1L);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            stationService.updateStation(station);
        });
        Assertions.assertEquals("İstasyon bulunamadı!", exception.getMessage());

    }
}
















