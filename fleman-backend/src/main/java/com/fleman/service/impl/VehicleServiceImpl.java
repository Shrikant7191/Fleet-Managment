package com.fleman.service.impl;

import com.fleman.dto.CarDTO;
import com.fleman.dto.CarTypeDTO;
import com.fleman.entity.Car;
import com.fleman.entity.Car.CarStatus;
import com.fleman.entity.BookingHeader.BookingStatus;
import com.fleman.entity.CarType;
import com.fleman.exception.ResourceNotFoundException;
import com.fleman.repository.CarRepository;
import com.fleman.repository.CarTypeRepository;
import com.fleman.service.VehicleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final CarRepository carRepository;
    private final CarTypeRepository carTypeRepository;

    public VehicleServiceImpl(CarRepository carRepository, CarTypeRepository carTypeRepository) {
        this.carRepository = carRepository;
        this.carTypeRepository = carTypeRepository;
    }

    @Override
    public List<CarTypeDTO> getCarTypes() {
        return carTypeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CarTypeDTO> getCarTypesForHub(Long hubId, LocalDateTime pickupDatetime, LocalDateTime returnDatetime) {
        if (hubId == null) return getCarTypes();
        // Reuses the same interval-overlap availability query the vehicle
        // list itself calls, then keeps only the car types that actually
        // have at least one available car left at this hub — a type with
        // zero available cars simply isn't offered, rather than showing up
        // as a dead end the customer can pick and then can't book.
        List<Car> cars = carRepository.findAvailableCars(hubId, pickupDatetime, returnDatetime,
                CarStatus.AVAILABLE, List.of(BookingStatus.CONFIRMED, BookingStatus.ONGOING));
        java.util.Set<Long> availableTypeIds = cars.stream().map(Car::getCarTypeId).collect(Collectors.toSet());
        return carTypeRepository.findAll().stream()
                .filter(ct -> availableTypeIds.contains(ct.getCarTypeId()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarDTO> getAvailableCars(Long hubId, LocalDateTime pickupDatetime, LocalDateTime returnDatetime) {
        if (hubId == null) {
            return carRepository.findAll().stream().map(c -> toDto(c, true)).collect(Collectors.toList());
        }
        // Two different questions, deliberately not conflated: "which cars
        // exist at this hub" (every one of them, so a just-booked car still
        // shows up - see bookableNow's javadoc on CarDTO) vs "which of them
        // can actually be booked for these dates" (the same interval-
        // overlap query the car-types filter also uses).
        List<Car> allAtHub = carRepository.findByHubId(hubId);
        java.util.Set<Long> bookableIds = carRepository.findAvailableCars(hubId, pickupDatetime, returnDatetime,
                        CarStatus.AVAILABLE, List.of(BookingStatus.CONFIRMED, BookingStatus.ONGOING))
                .stream().map(Car::getCarId).collect(Collectors.toSet());
        return allAtHub.stream()
                .map(c -> toDto(c, bookableIds.contains(c.getCarId())))
                .collect(Collectors.toList());
    }

    @Override
    public CarDTO getCarById(Long carId) {
        return carRepository.findById(carId).map(c -> toDto(c, c.getStatus() == CarStatus.AVAILABLE))
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + carId));
    }

    private CarDTO toDto(Car car, boolean bookableNow) {
        CarDTO dto = new CarDTO();
        dto.setCarId(car.getCarId());
        dto.setCarTypeId(car.getCarTypeId());
        dto.setHubId(car.getHubId());
        dto.setVehicleNumber(car.getVehicleNumber());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setManufactureYear(car.getManufactureYear());
        dto.setColor(car.getColor());
        dto.setFuelType(car.getFuelType() != null ? car.getFuelType().name() : null);
        dto.setSeatingCapacity(car.getSeatingCapacity());
        dto.setMileage(car.getMileage());
        dto.setOdometer(car.getOdometer());
        dto.setFuelLevel(car.getFuelLevel());
        dto.setAvailable(car.isAvailable());
        dto.setStatus(car.getStatus() != null ? car.getStatus().name() : null);
        dto.setBookableNow(bookableNow);
        // No relationship — an explicit second lookup by the plain
        // carTypeId column to build the nested CarTypeDTO the frontend expects.
        carTypeRepository.findById(car.getCarTypeId()).ifPresent(ct -> dto.setCarType(toDto(ct)));
        return dto;
    }

    private CarTypeDTO toDto(CarType ct) {
        CarTypeDTO dto = new CarTypeDTO();
        dto.setCarTypeId(ct.getCarTypeId());
        dto.setCarTypeName(ct.getCarTypeName());
        dto.setDailyRate(ct.getDailyRate());
        dto.setWeeklyRate(ct.getWeeklyRate());
        dto.setMonthlyRate(ct.getMonthlyRate());
        dto.setRateValidFrom(ct.getRateValidFrom());
        dto.setRateValidTo(ct.getRateValidTo());
        dto.setImagePath(ct.getImagePath());
        return dto;
    }
}
