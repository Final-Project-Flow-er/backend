package com.chaing.domain.transports.usecase.validator;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
public class TransportValidatorImpl implements TransportValidator {
    @Override
    public boolean canLoadWeight(Long maxLoad, Long currentTransitWeight) {

        long max = (maxLoad != null)? maxLoad:0L;
        long current = (currentTransitWeight != null)? currentTransitWeight:0L;

        return current < max;
    }
}
