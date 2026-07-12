package com.transitops.repository;

import com.transitops.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {
    List<MaintenanceLog> findByVehicleIdAndStatus(Long vehicleId, MaintenanceLog.MaintenanceStatus status);

    @Query("SELECT COALESCE(SUM(m.cost), 0) FROM MaintenanceLog m WHERE m.vehicle.id = :vehicleId")
    java.math.BigDecimal getTotalMaintenanceCostByVehicle(Long vehicleId);
}