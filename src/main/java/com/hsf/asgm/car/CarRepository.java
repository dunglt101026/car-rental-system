package com.hsf.asgm.car;

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


