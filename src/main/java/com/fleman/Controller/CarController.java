package com.fleman.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fleman.DTO.CarDTO;
import com.fleman.Service.CarService;

@RestController
@RequestMapping("/api/cars")
public class CarController {
	
	@Autowired
	private CarService carservice;
	
	@PostMapping
	public ResponseEntity<CarDTO> addCar(@RequestBody CarDTO dto)
	{
		CarDTO saved = carservice.addCar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}
}
