package com.hsf.asgm.car;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarMapper {

    @Mapping(target = "producerId", source = "producer.producerId")
    @Mapping(target = "producerName", source = "producer.producerName")
    CarResponse toResponse(Car car);

    CarProducerResponse toProducerResponse(CarProducer producer);
}

