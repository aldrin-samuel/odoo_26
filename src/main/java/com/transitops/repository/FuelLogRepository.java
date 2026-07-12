package com.transitops.repository;

import com.transitops.entity.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {
    List<FuelLog> findByVehicleId(Long vehicleId);

    @Query("SELECT COALESCE(SUM(f.totalCost), 0) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    BigDecimal getTotalFuelCostByVehicle(Long vehicleId);

    @Query("SELECT COALESCE(SUM(f.liters), 0) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    BigDecimal getTotalFuelLitersByVehicle(Long vehicleId);
}
