package com.hsf.fucarrenting.car.mapper;

import com.hsf.fucarrenting.car.dto.CarProducerResponse;
import com.hsf.fucarrenting.car.dto.CarRequest;
import com.hsf.fucarrenting.car.dto.CarResponse;
import com.hsf.fucarrenting.car.entity.Car;
import com.hsf.fucarrenting.car.entity.CarProducer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarMapper {

    @Mapping(target = "producerId", source = "producer.producerId")
    @Mapping(target = "producerName", source = "producer.producerName")
    CarResponse toResponse(Car car);

    CarProducerResponse toProducerResponse(CarProducer producer);

    @Mapping(target = "producerId", source = "producer.producerId")
    CarRequest toRequest(Car car);

    @Mapping(target = "carId", ignore = true)
    @Mapping(target = "producer", ignore = true)
    @Mapping(target = "carRentals", ignore = true)
    Car toEntity(CarRequest request);

    @Mapping(target = "carId", ignore = true)
    @Mapping(target = "producer", ignore = true)
    @Mapping(target = "carRentals", ignore = true)
    void updateEntityFromRequest(CarRequest request, @MappingTarget Car car);
}
