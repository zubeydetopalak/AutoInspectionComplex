package com.zubeyde.auto.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zubeyde.auto.entity.Admin;
import com.zubeyde.auto.entity.Appointment;
import com.zubeyde.auto.entity.AppointmentStatus;
import com.zubeyde.auto.entity.Brand;
import com.zubeyde.auto.entity.ChecklistTemplate;
import com.zubeyde.auto.entity.CriticalLevel;
import com.zubeyde.auto.entity.Customer;
import com.zubeyde.auto.entity.Station;
import com.zubeyde.auto.entity.Vehicle;
import com.zubeyde.auto.repository.AppointmentRepository;
import com.zubeyde.auto.repository.AuthRepository;
import com.zubeyde.auto.repository.BrandRepository;
import com.zubeyde.auto.repository.ChecklistTemplateRepository;
import com.zubeyde.auto.repository.CustomerRepository;
import com.zubeyde.auto.repository.StationRepository;
import com.zubeyde.auto.repository.VehicleRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(BrandRepository brandRepository, 
                               StationRepository stationRepository, 
                               ChecklistTemplateRepository checklistTemplateRepository, 
                               AuthRepository authRepository,
                               CustomerRepository customerRepository,
                               VehicleRepository vehicleRepository,
                               AppointmentRepository appointmentRepository) {
        return args -> {
            // 1. Init Brands
            if (brandRepository.count() == 0) {
                List<Brand> brands = Arrays.asList(
                    new Brand(null, "Toyota", "Japan"),
                    new Brand(null, "Volkswagen", "Germany"),
                    new Brand(null, "Ford", "USA"),
                    new Brand(null, "Honda", "Japan"),
                    new Brand(null, "BMW", "Germany"),
                    new Brand(null, "Mercedes-Benz", "Germany"),
                    new Brand(null, "Hyundai", "South Korea"),
                    new Brand(null, "Audi", "Germany"),
                    new Brand(null, "Nissan", "Japan"),
                    new Brand(null, "Chevrolet", "USA")
                );
                brandRepository.saveAll(brands);
                System.out.println("Default brands initialized.");
            }

            // 2. Init Stations
            if (stationRepository.count() == 0) {
                // Fetch some brands for exclusive stations
                Brand bmw = brandRepository.findByName("BMW").orElse(null);
                Brand toyota = brandRepository.findByName("Toyota").orElse(null);
                Brand mercedes = brandRepository.findByName("Mercedes-Benz").orElse(null);

                List<Station> stations = Arrays.asList(
                    // General Stations (No exclusive brand)
                    createStation("ST-GEN-01", null, true, 5),
                    createStation("ST-GEN-02", null, true, 5),
                    createStation("ST-GEN-03", null, true, 5),
                    createStation("ST-GEN-04", null, false, 3), // Closed one

                    // Exclusive Stations
                    createStation("ST-BMW-01", bmw, true, 3),
                    createStation("ST-TOY-01", toyota, true, 4),
                    createStation("ST-MER-01", mercedes, true, 3)
                );
                
                stationRepository.saveAll(stations);
                System.out.println("Default stations initialized.");
            }

            // 3. Init Checklist Templates
            if (checklistTemplateRepository.count() == 0) {
                ChecklistTemplate t1 = new ChecklistTemplate();
                t1.setDescription("Fren Sistemi Kontrolü");
                t1.setCategory("Fren");
                t1.setLevel(CriticalLevel.AGIR_KUSUR);
                t1.setVehicleType("Binek");
                checklistTemplateRepository.save(t1);

                ChecklistTemplate t2 = new ChecklistTemplate();
                t2.setDescription("Lastik Diş Derinliği");
                t2.setCategory("Lastik");
                t2.setLevel(CriticalLevel.AGIR_KUSUR);
                t2.setVehicleType("Binek");
                checklistTemplateRepository.save(t2);

                ChecklistTemplate t3 = new ChecklistTemplate();
                t3.setDescription("Far Ayarları");
                t3.setCategory("Aydınlatma");
                t3.setLevel(CriticalLevel.HAFIF_KUSUR);
                t3.setVehicleType("Binek");
                checklistTemplateRepository.save(t3);

                ChecklistTemplate t4 = new ChecklistTemplate();
                t4.setDescription("Silecekler");
                t4.setCategory("Görüş");
                t4.setLevel(CriticalLevel.HAFIF_KUSUR);
                t4.setVehicleType("Binek");
                checklistTemplateRepository.save(t4);
                
                ChecklistTemplate t5 = new ChecklistTemplate();
                t5.setDescription("Emniyet Kemeri");
                t5.setCategory("Güvenlik");
                t5.setLevel(CriticalLevel.AGIR_KUSUR);
                t5.setVehicleType("Binek");
                checklistTemplateRepository.save(t5);

                System.out.println("Default checklist templates initialized.");
            }

            // 4. Init Users
            if (authRepository.count() == 0) {
                Admin secretary = new Admin();
                secretary.setUsername("secretary");
                secretary.setPassword("password");
                secretary.setRole("SECRETARY");
                authRepository.save(secretary);
                
                Admin zub = new Admin();
                zub.setUsername("zub");
                zub.setPassword("zub");
                zub.setRole("INSPECTOR");
                authRepository.save(zub);

                Admin inspector = new Admin();
                inspector.setUsername("inspector");
                inspector.setPassword("password");
                inspector.setRole("INSPECTOR");
                authRepository.save(inspector);
                
                System.out.println("Default users initialized.");
            }

            // 5. Init Customer, Vehicle, Appointment
            if (customerRepository.count() == 0) {
                Customer customer = new Customer();
                customer.setName("Ahmet Yılmaz");
                customer.setPhone("5551234567");
                customer.setEmail("ahmet@example.com");
                customerRepository.save(customer);

                Brand toyota = brandRepository.findByName("Toyota").orElse(null);
                
                Vehicle vehicle = new Vehicle();
                vehicle.setPlateCode("34ABC123");
                vehicle.setModelYear("2020");
                vehicle.setChassisNumber("CH123456789");
                vehicle.setVehicleType("Binek");
                vehicle.setBrand(toyota);
                vehicle.setCustomer(customer);
                vehicleRepository.save(vehicle);

                Station station = stationRepository.findAll().stream()
                        .filter(s -> s.getExclusiveBrand() == null || s.getExclusiveBrand().getName().equals("Toyota"))
                        .findFirst().orElse(null);

                Appointment appointment = new Appointment();
                appointment.setVehicle(vehicle);
                appointment.setStation(station);
                appointment.setStatus(AppointmentStatus.PENDING);
                appointmentRepository.save(appointment);

                Customer customer2 = new Customer();
                customer2.setName("Levent Yılmaz");
                customer2.setPhone("5551234569");
                customer2.setEmail("levent@example.com");
                customerRepository.save(customer2);

                Brand toyota2 = brandRepository.findByName("Toyota").orElse(null);
                
                Vehicle vehicle2 = new Vehicle();
                vehicle2.setPlateCode("01ABC123");
                vehicle2.setModelYear("2020");
                vehicle2.setChassisNumber("CH123456789");
                vehicle2.setVehicleType("Binek");
                vehicle2.setBrand(toyota2);
                vehicle.setCustomer(customer2);
                vehicleRepository.save(vehicle2);

                Station station2 = stationRepository.findAll().stream()
                        .filter(s -> s.getExclusiveBrand() == null || s.getExclusiveBrand().getName().equals("Toyota"))
                        .findFirst().orElse(null);

                Appointment appointment2 = new Appointment();
                appointment2.setVehicle(vehicle2);
                appointment2.setStation(station2);
                appointment2.setStatus(AppointmentStatus.PENDING);
                appointmentRepository.save(appointment2);

                Customer customer3 = new Customer();
                customer3.setName("Ali Veli");
                customer3.setPhone("5551234369");
                customer3.setEmail("ali@example.com");
                customerRepository.save(customer3);

                Brand bmw = brandRepository.findByName("BMW").orElse(null);
                
                Vehicle veh = new Vehicle();
                veh.setPlateCode("04ABC123");
                veh.setModelYear("2020");
                veh.setChassisNumber("CH123456789");
                veh.setVehicleType("Binek");
                veh.setBrand(bmw);
                veh.setCustomer(customer3);
                vehicleRepository.save(veh);

                Station station3 = stationRepository.findAll().stream()
                        .filter(s -> s.getExclusiveBrand() == null || s.getExclusiveBrand().getName().equals("Toyota"))
                        .findFirst().orElse(null);

                Appointment appointment3 = new Appointment();
                appointment3.setVehicle(veh);
                appointment3.setStation(station3);
                appointment3.setStatus(AppointmentStatus.PENDING);
                appointmentRepository.save(appointment3);

                Customer cus = new Customer();
                cus.setName("Zeynep Yılmaz");
                cus.setPhone("5551934567");
                cus.setEmail("zeynep@example.com");
                customerRepository.save(cus);

                Brand chev = brandRepository.findByName("Chevrolet").orElse(null);
                
                Vehicle vehcus = new Vehicle();
                vehcus.setPlateCode("90ABC123");
                vehcus.setModelYear("2000");
                vehcus.setChassisNumber("CH123496789");
                vehcus.setVehicleType("Kamyonetnet");
                vehcus.setBrand(chev);
                vehcus.setCustomer(cus);
                vehicleRepository.save(vehcus);

                Station stationCus = stationRepository.findAll().stream()
                        .filter(s -> s.getExclusiveBrand() == null || s.getExclusiveBrand().getName().equals("Toyota"))
                        .findFirst().orElse(null);

                Appointment appointment5 = new Appointment();
                appointment5.setVehicle(vehcus);
                appointment5.setStation(stationCus);
                appointment5.setStatus(AppointmentStatus.PENDING);
                appointmentRepository.save(appointment5);

                Customer customer1 = new Customer();
                customer1.setName("Zübeyde Topalak");
                customer1.setPhone("5051934567");
                customer1.setEmail("zubu@example.com");
                customerRepository.save(customer1);

                Brand branma = brandRepository.findByName("Toyota").orElse(null);

                Vehicle vehicle1 = new Vehicle();
                vehicle1.setPlateCode("99ZZZ123");
                vehicle1.setModelYear("2000");
                vehicle1.setChassisNumber("CH123496789");
                vehicle1.setVehicleType("Binek");
                vehicle1.setBrand(branma);
                vehicle1.setCustomer(customer1);
                vehicleRepository.save(vehicle1);

                Station station1 = stationRepository.findAll().stream()
                        .filter(s -> s.getExclusiveBrand() == null || s.getExclusiveBrand().getName().equals("Toyota"))
                        .findFirst().orElse(null);

                Appointment appointment6 = new Appointment();
                appointment6.setVehicle(vehicle1);
                appointment6.setStation(station1);
                appointment6.setStatus(AppointmentStatus.PENDING);
                appointmentRepository.save(appointment6);


                System.out.println("Default customer, vehicle and appointment initialized.");
            }
        };
    }

    private Station createStation(String code, Brand brand, boolean isOpen, int capacity) {
        Station s = new Station();
        s.setStationCode(code);
        s.setExclusiveBrand(brand);
        s.setOpen(isOpen);
        s.setCapacity(capacity);
        return s;
    }
}
