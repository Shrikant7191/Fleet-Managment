package com.fleman.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fleman.DTO.CarTypeDTO;
import com.fleman.Service.CarTypeService;

@RestController
@RequestMapping("/api/vehicles")
public class CarTypeController {
	
	@Autowired
	private CarTypeService cartypeservice;
	
	
	@GetMapping("/types")
	public ResponseEntity<List<CarTypeDTO>> getVehicleTypes()
	{
		List<CarTypeDTO> types=cartypeservice.getAllCarTypes();
		return ResponseEntity.ok(types);
	}
	
}
  