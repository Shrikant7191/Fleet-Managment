package com.fleman.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fleman.Entity.Car;


public interface CarRepository extends JpaRepository<Car,Long>{

}
