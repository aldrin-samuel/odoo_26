// service/FuelService.java
package com.transitops.service;

import com.transitops.dto.FuelLogRequest;
import com.transitops.dto.FuelLogResponse;
import com.transitops.entity.FuelLog;
import com.transitops.entity.Trip;
import com.transitops.entity.Vehicle;
import com.transitops.repository.FuelLogRepository;
import com.transitops.repository.TripRepository;
import com.transitops.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelService {

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    public FuelLogResponse addFuelLog(FuelLogRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Trip trip = null;
        if (request.getTripId() != null) {
            trip = tripRepository.findById(request.getTripId())
                    .orElseThrow(() -> new RuntimeException("Trip not found"));
        }

        BigDecimal totalCost = request.getLiters().multiply(request.getCostPerLiter());

        FuelLog fuelLog = FuelLog.builder()
                .vehicle(vehicle)
                .trip(trip)
                .liters(request.getLiters())
                .costPerLiter(request.getCostPerLiter())
                .totalCost(totalCost)
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .odometerReading(request.getOdometerReading())
                .fuelStation(request.getFuelStation())
                .notes(request.getNotes())
                .build();

        fuelLog = fuelLogRepository.save(fuelLog);
        return toResponse(fuelLog);
    }

    public List<FuelLogResponse> getAllFuelLogs() {
        return fuelLogRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FuelLogResponse> getFuelLogsByVehicle(Long vehicleId) {
        return fuelLogRepository.findByVehicleId(vehicleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FuelLogResponse toResponse(FuelLog log) {
        return FuelLogResponse.builder()
                .id(log.getId())
                .vehicleId(log.getVehicle().getId())
                .vehicleName(log.getVehicle().getVehicleName())  // CORRECT METHOD
                .registrationNumber(log.getVehicle().getRegistrationNumber())
                .tripId(log.getTrip() != null ? log.getTrip().getId() : null)
                .tripNumber(log.getTrip() != null ? log.getTrip().getTripNumber() : null)
                .liters(log.getLiters())
                .costPerLiter(log.getCostPerLiter())
                .totalCost(log.getTotalCost())
                .date(log.getDate())
                .odometerReading(log.getOdometerReading())
                .fuelStation(log.getFuelStation())
                .build();
    }
}