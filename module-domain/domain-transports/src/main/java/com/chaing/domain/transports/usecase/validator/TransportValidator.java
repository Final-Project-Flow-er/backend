package com.chaing.domain.transports.usecase.validator;

import com.chaing.domain.transports.entity.Vehicle;

import java.util.List;

public interface TransportValidator {

    boolean canLoadWeight(Long maxLoad, Long currentTransitWeight);
}
