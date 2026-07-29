package com.fleman.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fleman.DTO.CarTypeDTO;
import com.fleman.Entity.*;
import com.fleman.Repository.CarTypeRepository;

@Service
public class CarTypeService {

	@Autowired
	private CarTypeRepository cartyperepository;
	
	private CarTypeDTO toDTO(CarType ct)
	{
		CarTypeDTO dto=new CarTypeDTO();
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
	
	public List<CarTypeDTO> getAllCarTypes()
	{
		return cartyperepository.findAll().stream().map(this::toDTO).toList();
	}
}
