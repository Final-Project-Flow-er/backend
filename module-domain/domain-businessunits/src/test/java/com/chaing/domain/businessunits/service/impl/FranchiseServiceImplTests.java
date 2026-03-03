package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.component.BusinessUnitCodeGenerator;
import com.chaing.domain.businessunits.component.DistanceCalculator;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceImplTests {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BusinessUnitCodeGenerator codeGenerator;

    @Mock
    private DistanceCalculator distanceCalculator;

    @InjectMocks
    private FranchiseServiceImpl franchiseService;

    @Test
    @DisplayName("가맹점 등록")
    void create() {

        // given
        String generatedCode = "SE01";
        Double calculatedDistance = 15.5;
        BusinessUnitCreateCommand command = new BusinessUnitCreateCommand(
                "신규 가맹점", "서울시 서초구", "010-1111-2222", "대표", "0123456",
                Region.SEOUL, new BusinessUnitCreateCommand.FranchiseCreate("월화수목금", LocalTime.of(9,0), LocalTime.of(22,0), "url"), null
        );

        when(codeGenerator.generateFranchiseCode(Region.SEOUL)).thenReturn(generatedCode);
        when(distanceCalculator.calculate(anyString())).thenReturn(calculatedDistance);

        // when
        BusinessUnitInternal result = franchiseService.create(command);

        // then
        assertNotNull(result);
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(distanceCalculator, times(1)).calculate("서울시 서초구");
    }

    @Test
    @DisplayName("가맹점 목록 조회")
    void getBusinessUnitList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Franchise> franchises = List.of(
                Franchise.builder().franchiseId(1L).build(),
                Franchise.builder().franchiseId(2L).build()
        );

        Page<Franchise> franchisePage = new PageImpl<>(franchises, pageable, franchises.size());
        when(franchiseRepository.findAll(pageable)).thenReturn(franchisePage);

        // when
        Page<BusinessUnitInternal> result = franchiseService.getBusinessUnitList(pageable);

        // then
        assertEquals(2, result.getContent().size());
        verify(franchiseRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("가맹점 조회")
    void getById() {

        // given
        Long id = 1L;
        Franchise franchise = Franchise.builder().franchiseId(id).name("가맹점").build();
        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        BusinessUnitInternal result = franchiseService.getById(id);

        // then
        assertNotNull(result);
        assertEquals("가맹점", result.name());
        verify(franchiseRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("가맹점 정보 수정")
    void updateInfo() {

        // given
        Long id = 1L;
        Franchise franchise = Franchise.builder().franchiseId(id).name("기존 가맹점").phone("010-1234-5678").warningCount(1).build();

        BusinessUnitUpdateCommand command = new BusinessUnitUpdateCommand(
                "변경된 가맹점", null, null, null, null, null,
                new BusinessUnitUpdateCommand.FranchiseUpdate(null, null, null, null, null, null),
                null
        );

        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.updateInfo(id, command);

        // then
        assertEquals("변경된 가맹점", franchise.getName());
        assertEquals("010-1234-5678", franchise.getPhone());
        assertEquals(1, franchise.getWarningCount());
    }

    @Test
    @DisplayName("가맹점 상태 변경")
    void updateStatus() {

        // given
        Long id = 1L;
        Franchise franchise = Franchise.builder().franchiseId(id).status(UsableStatus.ACTIVE).build();
        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.updateStatus(id, UsableStatus.INACTIVE);

        // then
        assertEquals(UsableStatus.INACTIVE, franchise.getStatus());
    }

    @Test
    @DisplayName("가맹점 삭제")
    void delete() {

        // given
        Long id = 1L;
        Franchise franchise = Franchise.builder().franchiseId(id).status(UsableStatus.ACTIVE).build();
        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.delete(id);

        // then
        verify(franchiseRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("가맹점 경고 부여 및 페널티")
    void addWarning() {

        // given
        Long id = 1L;
        Franchise franchise = spy(Franchise.builder().franchiseId(id).warningCount(2).build());
        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.addWarning(id);

        // then
        assertEquals(3, franchise.getWarningCount());
        assertNotNull(franchise.getPenaltyEndDate());
        verify(franchise, times(1)).addWarning();
    }
}