package com.chaing.domain.transports.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.VehicleCreateCommand;
import com.chaing.domain.transports.dto.command.VehicleUpdateCommand;
import com.chaing.domain.transports.dto.condition.VehicleSearchCondition;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.VehicleType;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VehicleManagementServiceTests {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleManagementService vehicleManagementService;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .vehicleNumber("12가 3456")
                .vehicleType(VehicleType.CARGO)
                .status(UsableStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("운송 차량 등록")
    void createVehicle() {

        // given
        VehicleCreateCommand command = mock(VehicleCreateCommand.class);
        given(command.vehicleNumber()).willReturn("12가 3456");
        given(vehicleRepository.save(any(Vehicle.class))).willReturn(vehicle);

        // when
        Vehicle result = vehicleManagementService.createVehicle(command);

        // then
        assertThat(result.getVehicleNumber()).isEqualTo("12가 3456");
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("운송 차량 목록 조회")
    void getVehicleList() {

        // given
        VehicleSearchCondition condition = new VehicleSearchCondition(null, null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(vehicle));
        given(vehicleRepository.searchVehicles(condition, pageable)).willReturn(vehiclePage);

        // when
        Page<Vehicle> result = vehicleManagementService.getVehicleList(condition, pageable);

        // then
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getVehicleNumber()).isEqualTo("12가 3456");
        verify(vehicleRepository, times(1)).searchVehicles(condition, pageable);
    }

    @Test
    @DisplayName("운송 차량 상세 조회")
    void getById() {

        // given
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        Vehicle result = vehicleManagementService.getById(1L);

        // then
        assertThat(result.getVehicleNumber()).isEqualTo("12가 3456");
    }

    @Test
    @DisplayName("존재하지 않는 차량 조회 시 예외 발생")
    void getById_Fail_NotFound() {

        // given
        given(vehicleRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> vehicleManagementService.getById(1L))
                .isInstanceOf(TransportException.class)
                .hasMessageContaining(TransportErrorCode.TRANSPORT_VEHICLE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("운송 차량 정보 수정")
    void updateVehicle() {

        // given
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));
        VehicleUpdateCommand updateCommand = mock(VehicleUpdateCommand.class);
        given(updateCommand.vehicleNumber()).willReturn("00나 0000");

        // when
        Vehicle result = vehicleManagementService.updateVehicle(1L, updateCommand);

        // then
        assertThat(result.getVehicleNumber()).isEqualTo("00나 0000");
    }

    @Test
    @DisplayName("운송 차량 상태 변경")
    void updateStatus() {

        // given
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        Vehicle result = vehicleManagementService.updateStatus(1L, UsableStatus.INACTIVE);

        // then
        assertThat(result.getStatus()).isEqualTo(UsableStatus.INACTIVE);
    }

    @Test
    @DisplayName("운송 차량 삭제")
    void deleteVehicle() {

        // given
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        vehicleManagementService.deleteVehicle(1L);

        // then
        assertThat(vehicle.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("운송 업체 상태 변경 시 소속 차량 일괄 상태 변경")
    void deactivateVehiclesByTransportId() {

        // given
        Long transportId = 1L;

        // when
        vehicleManagementService.deactivateVehiclesByTransportId(transportId);

        // then
        verify(vehicleRepository, times(1)).deactivateVehiclesByTransportId(transportId);
    }

    @Test
    @DisplayName("운송 업체 삭제 시 소속 차량 일괄 삭제")
    void deleteVehiclesByTransportId() {

        // given
        Long transportId = 1L;

        // when
        vehicleManagementService.deleteVehiclesByTransportId(transportId);

        // then
        verify(vehicleRepository, times(1)).deleteVehiclesByTransportId(transportId);
    }
}