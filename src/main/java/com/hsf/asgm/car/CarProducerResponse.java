package com.hsf.asgm.car;

import java.time.LocalDate;

public record CarProducerResponse(
        Integer producerId,
        String producerName,
        String address,
        String country
) {}

