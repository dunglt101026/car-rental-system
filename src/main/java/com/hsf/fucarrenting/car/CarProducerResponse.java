package com.hsf.fucarrenting.car;

public record CarProducerResponse(
        Integer producerId,
        String producerName,
        String address,
        String country
) {}

