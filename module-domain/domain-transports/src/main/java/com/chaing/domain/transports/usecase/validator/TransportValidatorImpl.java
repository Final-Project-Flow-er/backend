package com.chaing.domain.transports.usecase.validator;

import com.chaing.core.enums.UsableStatus;
import com.chaing.core.exception.ErrorCode;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
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

    @Override
    public void checkLoadable(Long maxLoad, Long currentWeight, Long newWeight) {

        long max = (maxLoad != null)? maxLoad:0L;
        long current = (currentWeight != null)? currentWeight:0L;
        long incoming = (newWeight != null)? newWeight:0L;

        if (current + incoming > max) {
            throw new TransportException(TransportErrorCode.TRANSPORT_LOAD_EXCEEDED);
        }
    }

    @Override
    public void checkCancellable(DeliverStatus status) {
        if(status != DeliverStatus.PENDING) {
            throw new TransportException((TransportErrorCode.TRANSPORT_CAN_NOT_CANCEL));
        }
    }
}
