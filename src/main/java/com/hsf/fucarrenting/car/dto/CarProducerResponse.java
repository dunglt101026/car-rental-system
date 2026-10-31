package com.hsf.fucarrenting.car.dto;

public record CarProducerResponse(
        Integer producerId,
        String producerName,
        String address,
        String country
) {}

