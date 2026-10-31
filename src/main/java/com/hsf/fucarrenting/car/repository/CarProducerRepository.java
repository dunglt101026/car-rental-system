package com.hsf.fucarrenting.car.repository;

import com.hsf.fucarrenting.car.entity.CarProducer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarProducerRepository extends JpaRepository<CarProducer, Integer> {
    List<CarProducer> findAllByOrderByProducerNameAsc();
}


