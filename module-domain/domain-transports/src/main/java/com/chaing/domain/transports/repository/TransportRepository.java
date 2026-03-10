package com.chaing.domain.transports.repository;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransportRepository extends JpaRepository<Transport, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Transport t SET t.status = :status WHERE t.transportId = :id AND t.deletedAt IS NULL")
    void updateStatus(@Param("id") Long id, @Param("status") UsableStatus status);
}
