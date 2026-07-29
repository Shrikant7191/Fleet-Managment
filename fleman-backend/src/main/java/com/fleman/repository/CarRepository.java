package com.fleman.repository;

import com.fleman.entity.Car;
import com.fleman.entity.Car.CarStatus;
import com.fleman.entity.BookingHeader.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByHubId(Long hubId);

    /**
     * The availability query for the vehicle-selection screen. No JPA
     * relationships are used here — every join condition below matches
     * plain id columns directly (c.hubId, b.carId), since this project
     * doesn't use database-level foreign keys or entity associations
     * anywhere.
     *
     * A car at the requested hub is "available" for a date range when:
     *   1. its status is AVAILABLE, and
     *   2. it is NOT referenced by any CONFIRMED/ONGOING booking whose own
     *      [pickupDatetime, returnDatetime) window overlaps the requested one.
     *
  
     */
    @Query("""
        SELECT c FROM Car c
        WHERE c.hubId = :hubId
          AND c.status = :availableStatus
          AND (:pickupDatetime IS NULL OR :returnDatetime IS NULL OR c.carId NOT IN (
              SELECT b.carId FROM BookingHeader b
              WHERE b.bookingStatus IN :activeStatuses
                AND b.pickupDatetime < :returnDatetime
                AND b.returnDatetime > :pickupDatetime
          ))
        """)
    List<Car> findAvailableCars(
            @Param("hubId") Long hubId,
            @Param("pickupDatetime") LocalDateTime pickupDatetime,
            @Param("returnDatetime") LocalDateTime returnDatetime,
            @Param("availableStatus") CarStatus availableStatus,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );

    /**
     * Per-car-type fleet counts, optionally scoped to one hub — the other
     * custom query in this repository (findAvailableCars above). Backs two
     * features: hiding a car type from the vehicle-selection page when a
     * hub has zero available cars of that type, and the staff dashboard's
     * "X available / Y handed over / Z in maintenance" breakdown per type.
     */
    @Query("""
        SELECT c.carTypeId AS carTypeId,
               COUNT(c) AS totalCars,
               SUM(CASE WHEN c.status = :available THEN 1L ELSE 0L END) AS availableCars,
               SUM(CASE WHEN c.status = :booked THEN 1L ELSE 0L END) AS bookedCars,
               SUM(CASE WHEN c.status = :maintenance THEN 1L ELSE 0L END) AS maintenanceCars
        FROM Car c
        WHERE (:hubId IS NULL OR c.hubId = :hubId)
        GROUP BY c.carTypeId
        """)
    List<CarTypeCountProjection> countByCarType(
            @Param("hubId") Long hubId,
            @Param("available") CarStatus available,
            @Param("booked") CarStatus booked,
            @Param("maintenance") CarStatus maintenance
    );

    // Interface-based projection for the aggregate query above — Spring
    // Data JPA matches these getters to the query's "AS" aliases.
    interface CarTypeCountProjection {
        Long getCarTypeId();
        Long getTotalCars();
        Long getAvailableCars();
        Long getBookedCars();
        Long getMaintenanceCars();
    }
}
