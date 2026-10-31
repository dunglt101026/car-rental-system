package com.hsf.fucarrenting.car.repository;

import com.hsf.fucarrenting.car.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer>, JpaSpecificationExecutor<Car> {
    List<Car> findByStatus(String status);
    List<Car> findByStatusOrderByCarIdDesc(String status);
    List<Car> findByProducer_ProducerId(Integer producerId);
}


