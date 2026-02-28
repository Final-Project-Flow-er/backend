package com.chaing.domain.transports.dto.response;

import com.chaing.domain.transports.entity.Vehicle;

import java.util.List;

public record AvailableVehicleResponse(

) {
    public static AvailableVehicleResponse from(List<Vehicle> availableVehicles) {
        return null;
    }
}
