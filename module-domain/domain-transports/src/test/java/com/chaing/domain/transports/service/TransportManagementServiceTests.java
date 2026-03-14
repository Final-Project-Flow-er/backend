package com.chaing.domain.transports.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.TransportCreateCommand;
import com.chaing.domain.transports.dto.command.TransportUpdateCommand;
import com.chaing.domain.transports.dto.condition.TransportSearchCondition;
import com.chaing.domain.transports.entity.Transport;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.TransportRepository;
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

import java.time.LocalDate;
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
class TransportManagementServiceTests {

    @Mock
    private TransportRepository transportRepository;

    @InjectMocks
    private TransportManagementService transportManagementService;

    private Transport transport;

    @BeforeEach
    void setUp() {
        transport = Transport.builder()
                .companyName("업체")
                .officePhone("010-1234-5678")
                .status(UsableStatus.ACTIVE)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusYears(1))
                .build();
    }

    @Test
    @DisplayName("운송 업체 등록")
    void createTransport() {

        // given
        TransportCreateCommand command = mock(TransportCreateCommand.class);
        given(command.companyName()).willReturn("업체");
        given(transportRepository.save(any(Transport.class))).willReturn(transport);

        // when
        Transport result = transportManagementService.createTransport(command);

        // then
        assertThat(result.getCompanyName()).isEqualTo("업체");
        verify(transportRepository, times(1)).save(any(Transport.class));
    }

    @Test
    @DisplayName("운송 업체 목록 조회")
    void getTransportList() {

        // given
        TransportSearchCondition condition = new TransportSearchCondition(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transport> transportPage = new PageImpl<>(List.of(transport));
        given(transportRepository.searchTransports(condition, pageable)).willReturn(transportPage);

        // when
        Page<Transport> result = transportManagementService.getTransportList(condition, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(transportRepository, times(1)).searchTransports(condition, pageable);
    }

    @Test
    @DisplayName("운송 업체 상세 조회")
    void getById() {

        // given
        given(transportRepository.findById(1L)).willReturn(Optional.of(transport));

        // when
        Transport result = transportManagementService.getById(1L);

        // then
        assertThat(result.getCompanyName()).isEqualTo("업체");
    }

    @Test
    @DisplayName("존재하지 않는 업체 조회 시 예외 발생")
    void getById_Fail_NotFound() {

        // given
        given(transportRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> transportManagementService.getById(1L))
                .isInstanceOf(TransportException.class)
                .hasMessageContaining(TransportErrorCode.TRANSPORT_VENDOR_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("운송 업체 정보 수정")
    void updateTransport() {

        // given
        Long id = 1L;
        given(transportRepository.findById(id)).willReturn(Optional.of(transport));
        TransportUpdateCommand updateCommand = mock(TransportUpdateCommand.class);
        given(updateCommand.companyName()).willReturn("수정된 업체");
        given(updateCommand.contractStartDate()).willReturn(LocalDate.now());
        given(updateCommand.contractEndDate()).willReturn(LocalDate.now().plusYears(1));

        // when
        Transport result = transportManagementService.updateTransport(id, updateCommand);

        // then
        assertThat(result.getCompanyName()).isEqualTo("수정된 업체");
    }

    @Test
    @DisplayName("운송 업체 상태 변경")
    void updateStatus() {

        // given
        Long id = 1L;
        UsableStatus newStatus = UsableStatus.INACTIVE;
        given(transportRepository.findById(id)).willReturn(Optional.of(transport));

        // when
        transportManagementService.updateStatus(id, newStatus);

        // then
        verify(transportRepository).updateStatus(id, newStatus);
    }

    @Test
    @DisplayName("운송 업체 삭제")
    void deleteTransport() {

        // given
        Long id = 1L;

        // when
        transportManagementService.deleteTransport(id);

        // then
        verify(transportRepository, times(1)).softDeleteById(id);
    }

    @Test
    @DisplayName("계약 만료 업체 자동 비활성화")
    void deactivateExpiredContracts() {

        // given
        LocalDate today = LocalDate.now();
        given(transportRepository.findAllByContractEndDateBeforeAndStatus(today, UsableStatus.ACTIVE)).willReturn(List.of(transport));

        // when
        List<Long> expiredIds = transportManagementService.deactivateExpiredContractsAndGetIds();

        // then
        assertThat(expiredIds).asList().hasSize(1);
        assertThat(expiredIds).asList().contains(transport.getTransportId());
        verify(transportRepository).deactivateTransportsByIds(expiredIds);
    }
}