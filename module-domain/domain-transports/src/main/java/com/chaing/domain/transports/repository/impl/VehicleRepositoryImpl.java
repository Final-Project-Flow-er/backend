package com.chaing.domain.transports.repository.impl;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.condition.VehicleSearchCondition;
import com.chaing.domain.transports.entity.QVehicle;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;
import com.chaing.domain.transports.repository.interfaces.VehicleRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class VehicleRepositoryImpl implements VehicleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Vehicle> searchVehicles(VehicleSearchCondition condition, Pageable pageable) {

        QVehicle vehicle = QVehicle.vehicle;

        List<Vehicle> content = queryFactory
                .selectFrom(vehicle)
                .where(
                        transportIdEq(condition.transportId()),
                        vehicleNumberContains(condition.vehicleNumber()),
                        vehicleTypeEq(condition.vehicleType()),
                        maxLoadGoe(condition.maxLoad()),
                        dispatchableEq(condition.dispatchable()),
                        statusEq(condition.status()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(vehicle.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(vehicle.count())
                .from(vehicle)
                .where(
                        transportIdEq(condition.transportId()),
                        vehicleNumberContains(condition.vehicleNumber()),
                        vehicleTypeEq(condition.vehicleType()),
                        maxLoadGoe(condition.maxLoad()),
                        dispatchableEq(condition.dispatchable()),
                        statusEq(condition.status()))
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression transportIdEq(Long transportId) {
        return transportId != null ? QVehicle.vehicle.transportId.eq(transportId) : null;
    }

    private BooleanExpression vehicleNumberContains(String vehicleNumber) {
        return hasText(vehicleNumber) ? QVehicle.vehicle.vehicleNumber.contains(vehicleNumber) : null;
    }

    private BooleanExpression vehicleTypeEq(VehicleType vehicleType) {
        return vehicleType != null ? QVehicle.vehicle.vehicleType.eq(vehicleType) : null;
    }

    private BooleanExpression maxLoadGoe(Long maxLoad) {
        return maxLoad != null ? QVehicle.vehicle.maxLoad.goe(maxLoad) : null;
    }

    private BooleanExpression dispatchableEq(Dispatchable dispatchable) {
        return dispatchable != null ? QVehicle.vehicle.dispatchable.eq(dispatchable) : null;
    }

    private BooleanExpression statusEq(UsableStatus status) {
        return status != null ? QVehicle.vehicle.status.eq(status) : null;
    }
}
