package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.TransportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportLogRepository extends JpaRepository<TransportLog,Long> {

}
