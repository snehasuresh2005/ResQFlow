package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.location.Shelter;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.request.ResourceRequirement;
import com.resqflow.domain.resource.FoodResource;
import com.resqflow.domain.resource.MedicalResource;
import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.resource.WaterResource;
import com.resqflow.domain.routing.Road;
import com.resqflow.domain.user.Role;
import com.resqflow.domain.user.User;
import com.resqflow.domain.vehicle.Driver;
import com.resqflow.domain.vehicle.Truck;
import com.resqflow.domain.vehicle.Van;
import com.resqflow.domain.vehicle.Vehicle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepotRepository depotRepository;
    private final EmergencyZoneRepository zoneRepository;
    private final ShelterRepository shelterRepository;
    private final ResourceRepository resourceRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RoadRepository roadRepository;
    private final EmergencyRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, DepotRepository depotRepository,
                          EmergencyZoneRepository zoneRepository, ShelterRepository shelterRepository,
                          ResourceRepository resourceRepository, VehicleRepository vehicleRepository,
                          DriverRepository driverRepository, RoadRepository roadRepository,
                          EmergencyRequestRepository requestRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.depotRepository = depotRepository;
        this.zoneRepository = zoneRepository;
        this.shelterRepository = shelterRepository;
        this.resourceRepository = resourceRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.roadRepository = roadRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Already seeded
        }

        Random random = new Random();

        // 1. Seed Users (5 roles)
        List<User> users = List.of(
                User.builder().name("Admin User").email("admin@resqflow.com").passwordHash(passwordEncoder.encode("password")).role(Role.ADMIN).active(true).build(),
                User.builder().name("Coordinator User").email("coordinator@resqflow.com").passwordHash(passwordEncoder.encode("password")).role(Role.COORDINATOR).active(true).build(),
                User.builder().name("Driver User").email("driver@resqflow.com").passwordHash(passwordEncoder.encode("password")).role(Role.DRIVER).active(true).build(),
                User.builder().name("Volunteer User").email("volunteer@resqflow.com").passwordHash(passwordEncoder.encode("password")).role(Role.VOLUNTEER).active(true).build(),
                User.builder().name("Viewer User").email("viewer@resqflow.com").passwordHash(passwordEncoder.encode("password")).role(Role.VIEWER).active(true).build()
        );
        userRepository.saveAll(users);

        // 2. Seed Depots (10 locations)
        List<Depot> depots = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            depots.add(depotRepository.save(Depot.builder()
                    .name("Central Depot " + i)
                    .latitude(12.90 + (i * 0.02))
                    .longitude(77.55 + (i * 0.015))
                    .capacity(5000.0)
                    .build()));
        }

        // 3. Seed Shelters (5 locations)
        List<Shelter> shelters = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            shelters.add(shelterRepository.save(Shelter.builder()
                    .name("Safe Shelter " + i)
                    .latitude(12.92 + (i * 0.018))
                    .longitude(77.58 + (i * 0.02))
                    .capacity(200)
                    .currentOccupancy(0)
                    .build()));
        }

        // 4. Seed Emergency Zones (20 locations)
        List<EmergencyZone> zones = new ArrayList<>();
        String[] scenarios = {"Flood Zone", "Earthquake Zone", "Wildfire Zone", "Landslide Zone"};
        for (int i = 1; i <= 20; i++) {
            String name = scenarios[i % scenarios.length] + " " + i;
            String sev = i % 5 == 0 ? "CRITICAL" : (i % 3 == 0 ? "HIGH" : "MEDIUM");
            zones.add(zoneRepository.save(EmergencyZone.builder()
                    .name(name)
                    .latitude(12.88 + (i * 0.012))
                    .longitude(77.52 + (i * 0.018))
                    .severity(sev)
                    .populationAffected(50 + random.nextInt(350))
                    .build()));
        }

        // 5. Seed Resources (100 batches)
        List<Resource> resources = new ArrayList<>();
        String[] resourceNames = {"High-energy Food Ration", "Mineral Water Bottle", "First-Aid Emergency Kit", "Blanket & Warmth Pack", "Generator Utility Kit"};
        String[] units = {"units", "litres", "kits", "kits", "units"};
        
        for (int i = 1; i <= 100; i++) {
            Depot depot = depots.get(i % depots.size());
            int idx = i % resourceNames.length;
            
            Resource res;
            if (idx == 0) {
                FoodResource food = new FoodResource();
                food.setStorageRequirement("DRY");
                res = food;
            } else if (idx == 2) {
                MedicalResource medical = new MedicalResource();
                medical.setTempConstraint(4.0); // Refrigerated medical kits
                res = medical;
            } else {
                res = new WaterResource();
            }

            res.setName(resourceNames[idx]);
            res.setResourceType(idx == 0 ? "FOOD" : (idx == 1 ? "WATER" : (idx == 2 ? "MEDICAL" : (idx == 3 ? "SHELTER" : "EQUIPMENT"))));
            res.setQuantity(100.0 + random.nextInt(400));
            res.setUnit(units[idx]);
            res.setDepot(depot);
            res.setExpiryDate(LocalDate.now().plusDays(10 + random.nextInt(180)));
            res.setPriority(random.nextInt(5));
            res.setWeightPerUnit(1.2);
            res.setVolumePerUnit(0.6);
            res.setStatus("AVAILABLE");

            resources.add(resourceRepository.save(res));
        }

        // 6. Seed Vehicles (20 units)
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Vehicle vehicle;
            if (i % 3 == 0) {
                vehicle = new Van();
            } else {
                vehicle = new Truck();
            }

            vehicle.setRegistrationNumber("KA-0" + (10 + i) + "-EQ" + (100 + i));
            vehicle.setCapacityWeight(1000.0 + (i * 100.0));
            vehicle.setCapacityVolume(500.0 + (i * 50.0));
            vehicle.setFuelLevel(80.0);
            vehicle.setCurrentLatitude(12.95 + (i * 0.005));
            vehicle.setCurrentLongitude(77.60 + (i * 0.004));
            vehicle.setStatus("AVAILABLE");

            vehicles.add(vehicleRepository.save(vehicle));
        }

        // 7. Seed Drivers (10 drivers, linked to 10 vehicles)
        for (int i = 1; i <= 10; i++) {
            Vehicle vehicle = vehicles.get(i - 1);
            driverRepository.save(Driver.builder()
                    .name("Relief Driver " + i)
                    .vehicle(vehicle)
                    .status("AVAILABLE")
                    .build());
        }

        // 8. Seed Roads (connect depots, shelters, zones - ~50 roads)
        // Let's create realistic graph road connections
        for (int i = 0; i < 20; i++) {
            Depot depot = depots.get(i % depots.size());
            EmergencyZone zone = zones.get(i);
            
            // Connect Depot to Zone
            roadRepository.save(Road.builder()
                    .name("Highway DEPOT-ZONE-" + i)
                    .startNodeType("DEPOT")
                    .startNodeId(depot.getId())
                    .endNodeType("ZONE")
                    .endNodeId(zone.getId())
                    .distance(5.0 + (i * 1.5))
                    .travelTime(10.0 + (i * 2.0))
                    .status("OPEN")
                    .build());

            // Connect Zone to nearest Shelter
            Shelter shelter = shelters.get(i % shelters.size());
            roadRepository.save(Road.builder()
                    .name("Rescue Road ZONE-SHELTER-" + i)
                    .startNodeType("ZONE")
                    .startNodeId(zone.getId())
                    .endNodeType("SHELTER")
                    .endNodeId(shelter.getId())
                    .distance(3.0 + (i * 0.8))
                    .travelTime(6.0 + (i * 1.2))
                    .status("OPEN")
                    .build());
        }

        // 9. Seed Emergency Requests (50 requests)
        for (int i = 1; i <= 50; i++) {
            EmergencyZone zone = zones.get(i % zones.size());
            String p = i % 10 == 0 ? "CRITICAL" : (i % 3 == 0 ? "HIGH" : "MEDIUM");
            String type = i % 3 == 0 ? "MEDICAL" : (i % 2 == 0 ? "WATER" : "FOOD");

            EmergencyRequest request = EmergencyRequest.builder()
                    .requestNumber("REQ-100" + i)
                    .emergencyZone(zone)
                    .requestType(type)
                    .priority(p)
                    .numberOfPeopleAffected(40 + random.nextInt(160))
                    .deadline(LocalDateTime.now().plusHours(2 + random.nextInt(12)))
                    .status("CREATED")
                    .build();

            List<ResourceRequirement> requirements = List.of(
                    ResourceRequirement.builder()
                            .request(request)
                            .resourceType(type)
                            .quantity(50.0 + random.nextInt(150))
                            .unit("units")
                            .build()
            );
            request.setRequestedResources(requirements);
            requestRepository.save(request);
        }
    }
}
