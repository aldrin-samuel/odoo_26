// service/MaintenanceService.java
package com.transitops.service;

import com.transitops.dto.MaintenanceLogRequest;
import com.transitops.dto.MaintenanceLogResponse;
import com.transitops.entity.MaintenanceLog;
import com.transitops.entity.Vehicle;
import com.transitops.entity.enums.VehicleStatus;  // CORRECT IMPORT
import com.transitops.repository.MaintenanceLogRepository;
import com.transitops.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public MaintenanceLogResponse createMaintenance(MaintenanceLogRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        MaintenanceLog maintenanceLog = MaintenanceLog.builder()
                .vehicle(vehicle)
                .description(request.getDescription())
                .type(request.getType())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .cost(request.getCost())
                .status(MaintenanceLog.MaintenanceStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        maintenanceLog = maintenanceLogRepository.save(maintenanceLog);

        // Business Rule: Auto change vehicle status to IN_SHOP
        if (vehicle.getStatus() != VehicleStatus.RETIRED) {  // USE IMPORTED ENUM
            vehicle.setStatus(VehicleStatus.IN_SHOP);
            vehicleRepository.save(vehicle);
        }

        return toResponse(maintenanceLog);
    }

    @Transactional
    public MaintenanceLogResponse completeMaintenance(Long id) {
        MaintenanceLog maintenanceLog = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance log not found"));

        maintenanceLog.setStatus(MaintenanceLog.MaintenanceStatus.COMPLETED);
        maintenanceLog.setEndDate(LocalDate.now());
        maintenanceLog = maintenanceLogRepository.save(maintenanceLog);

        // Business Rule: Restore vehicle to Available (unless retired)
        Vehicle vehicle = maintenanceLog.getVehicle();
        if (vehicle.getStatus() != VehicleStatus.RETIRED) {  // USE IMPORTED ENUM
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return toResponse(maintenanceLog);
    }

    public List<MaintenanceLogResponse> getAllMaintenance() {
        return maintenanceLogRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MaintenanceLogResponse> getActiveMaintenanceByVehicle(Long vehicleId) {
        return maintenanceLogRepository.findByVehicleIdAndStatus(vehicleId, MaintenanceLog.MaintenanceStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MaintenanceLogResponse toResponse(MaintenanceLog log) {
        return MaintenanceLogResponse.builder()
                .id(log.getId())
                .vehicleId(log.getVehicle().getId())
                .vehicleName(log.getVehicle().getVehicleName())  // CORRECT METHOD NAME
                .registrationNumber(log.getVehicle().getRegistrationNumber())
                .description(log.getDescription())
                .type(log.getType())
                .startDate(log.getStartDate())
                .endDate(log.getEndDate())
                .cost(log.getCost())
                .status(log.getStatus().name())
                .notes(log.getNotes())
                .build();
    }
}