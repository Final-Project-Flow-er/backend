package com.chaing.domain.transports.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.TransportCreateCommand;
import com.chaing.domain.transports.dto.command.TransportUpdateCommand;
import com.chaing.domain.transports.entity.Transport;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportManagementService {

    private final TransportRepository transportRepository;

    // 운송 업체 등록
    public Transport createTransport(TransportCreateCommand command) {
        Transport transport = Transport.createTransport(command);
        return transportRepository.save(transport);
    }

    // 운송 업체 목록 조회
    public Page<Transport> getTransportList(Pageable pageable) {
        return transportRepository.findAll(pageable);
    }

    // 운송 업체 상세 조회
    public Transport getById(Long id) {
        return transportRepository.findById(id)
                .orElseThrow(() -> new TransportException(TransportErrorCode.TRANSPORT_VENDOR_NOT_FOUND));
    }

    // 운송 업체 수정
    public Transport updateTransport(Long id, TransportUpdateCommand command) {
        Transport transport = getById(id);
        transport.updateTransport(command);
        return transport;
    }

    // 운송 업체 상태 변경
    public Transport updateStatus(Long id, UsableStatus status) {
        transportRepository.updateStatus(id, status);
        return getById(id);
    }

    // 운송 업체 삭제
    public void deleteTransport(Long id) {
        transportRepository.softDeleteById(id);
    }

    // 계약 기간 만료 시 자동 비활성화
    public List<Long> deactivateExpiredContractsAndGetIds() {
        LocalDate today = LocalDate.now();
        List<Transport> expiredTransports = transportRepository.findAllByContractEndDateBeforeAndStatus(today, UsableStatus.ACTIVE);
        List<Long> expiredIds = expiredTransports.stream().map(Transport::getTransportId).toList();

        if (!expiredIds.isEmpty()) {
            transportRepository.deactivateTransportsByIds(expiredIds);
        }
        return expiredIds;
    }
}
