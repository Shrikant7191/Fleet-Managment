package com.fleman.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fleman.Entity.*;


public interface CarTypeRepository extends JpaRepository<CarType, Long> {

}
