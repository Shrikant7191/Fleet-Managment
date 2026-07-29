package com.fleman.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fleman.DTO.CarDTO;
import com.fleman.DTO.CarTypeDTO;
import com.fleman.Entity.Car;
import com.fleman.Entity.CarType;
import com.fleman.Repository.CarRepository;
import com.fleman.Repository.CarTypeRepository;

@Service
public class CarService {

	@Autowired 
	private CarRepository carRepository;

	@Autowired
	private CarTypeRepository cartyperepository;
	
	private CarDTO toDTO(Car car)
	{
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

        cartyperepository.findById(car.getCarTypeId())
                .ifPresent(ct -> dto.setCarType(toCarTypeDTO(ct)));
		return dto;
	}
	
	private CarTypeDTO toCarTypeDTO(CarType ct) {
        CarTypeDTO dto = new CarTypeDTO();
        dto.setCarTypeId(ct.getCarTypeId());
        dto.setCarTypeName(ct.getCarTypeName());
        dto.setDailyRate(ct.getDailyRate());
        dto.setWklyRate(ct.getWklyRate());
        dto.setMnthRate(ct.getMnthRate());
        dto.setRateValidFrom(ct.getRateValidFrom());
        dto.setRateValidTo(ct.getRateValidTo());
        dto.setImagePath(ct.getImagePath());
        return dto;
    }
	
	private Car toEntity(CarDTO dto) {
        Car car = new Car();
        car.setCarTypeId(dto.getCarTypeId());
        car.setHubId(dto.getHubId());
        car.setVehicleNumber(dto.getVehicleNumber());
        car.setBrand(dto.getBrand());
        car.setModel(dto.getModel());
        car.setManufactureYear(dto.getManufactureYear());
        car.setColor(dto.getColor());
        if (dto.getFuelType() != null) {
            car.setFuelType(Car.FuelType.valueOf(dto.getFuelType().toUpperCase()));
        }
        car.setSeatingCapacity(dto.getSeatingCapacity());
        car.setMileage(dto.getMileage());
        car.setOdometer(dto.getOdometer());
        car.setFuelLevel(dto.getFuelLevel());
        car.setAvailable(dto.isAvailable());
        if (dto.getStatus() != null) {
            car.setStatus(Car.CarStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        return car;
    }
	
	 public CarDTO addCar(CarDTO dto) {
	        Car car = toEntity(dto);          // step 1: build entity from incoming JSON
	        Car saved = carRepository.save(car); // step 2: INSERT into DB, get generated carId
	        return toDTO(saved);              // step 3: build response DTO with nested carType
	    }
	
}
