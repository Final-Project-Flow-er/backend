package com.chaing.domain.businessunits.service.impl;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Headquarter;
import com.chaing.domain.businessunits.repository.HeadquarterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeadquarterServiceImplTests {

    @Mock
    private HeadquarterRepository headquarterRepository;

    @InjectMocks
    private HeadquarterServiceImpl headquarterService;

    @Test
    @DisplayName("본사 조회")
    void getById() {

        // given
        Long id = 1L;
        Headquarter hq = Headquarter.builder().hqId(id).name("본사").representativeName("대표").build();
        when(headquarterRepository.findById(id)).thenReturn(Optional.of(hq));

        // when
        BusinessUnitInternal result = headquarterService.getById(id);

        // then
        assertNotNull(result);
        assertEquals("본사", result.name());
        assertEquals("대표", result.representativeName());
        verify(headquarterRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("본사 정보 수정")
    void updateInfo() {

        // given
        Long id = 1L;
        Headquarter hq = Headquarter.builder().hqId(id).name("기존 본사").address("서울시 서초구").phone("02-123-4567").build();

        BusinessUnitUpdateCommand command = new BusinessUnitUpdateCommand(
                "변경된 본사", null, null, null, null,
                null, null, null
        );
        when(headquarterRepository.findById(id)).thenReturn(Optional.of(hq));

        // when
        BusinessUnitInternal result = headquarterService.updateInfo(id, command);

        // then
        assertNotNull(result);
        assertEquals("변경된 본사", hq.getName());
        assertEquals("서울시 서초구", hq.getAddress());
        assertEquals("02-123-4567", hq.getPhone());

        verify(headquarterRepository, times(1)).findById(id);
    }
}