package com.chaing.domain.transports.repository.interfaces;

import com.chaing.domain.transports.dto.condition.TransportSearchCondition;
import com.chaing.domain.transports.entity.Transport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransportRepositoryCustom {

    Page<Transport> searchTransports(TransportSearchCondition condition, Pageable pageable);
}
