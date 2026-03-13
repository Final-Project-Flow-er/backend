package com.chaing.domain.transports.repository;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transport;
import com.chaing.domain.transports.repository.interfaces.TransportRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransportRepository extends JpaRepository<Transport, Long>, TransportRepositoryCustom {

    List<Transport> findAllByContractEndDateBeforeAndStatus(LocalDate today, UsableStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Transport t SET t.status = 'INACTIVE' WHERE t.transportId IN :ids")
    void deactivateTransportsByIds(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Transport t SET t.status = :status WHERE t.transportId = :id AND t.deletedAt IS NULL")
    void updateStatus(@Param("id") Long id, @Param("status") UsableStatus status);

    @Query("select t.unitPrice from Transport t where t.transportId = :transportId")
    Long findUnitPriceByTransportId(@Param("transportId") Long transportId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Transport t SET t.deletedAt = CURRENT_TIMESTAMP, t.status = 'INACTIVE' WHERE t.transportId = :id")
    void softDeleteById(@Param("id") Long id);

    @Query("select t.companyName from Transport t where t.transportId = :transportId")
    String findCompanyNameByTransportId(@Param("transportId") Long transportId);
}
