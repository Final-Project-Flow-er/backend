package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.TransportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportLogRepository extends JpaRepository<TransportLog,Long> {

    @Query("select tl from TransportLog tl")
    List<TransportLog> getAll();
}
