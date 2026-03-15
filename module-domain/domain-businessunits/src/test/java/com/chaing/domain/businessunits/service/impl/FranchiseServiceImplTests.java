package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.component.BusinessUnitCodeGenerator;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceImplTests {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BusinessUnitCodeGenerator codeGenerator;

    @InjectMocks
    private FranchiseServiceImpl franchiseService;

    @Test
    @DisplayName("가맹점 등록")
    void create() {

        // given
        String generatedCode = "SE01";
        BusinessUnitCreateCommand command = new BusinessUnitCreateCommand(
                "신규 가맹점", "서울시 서초구", "010-1111-2222", "대표", "0123456",
                Region.SEOUL, new BusinessUnitCreateCommand.FranchiseCreate("월화수목금", LocalTime.of(9,0), LocalTime.of(22,0)), null
        );

        when(codeGenerator.generateFranchiseCode(Region.SEOUL)).thenReturn(generatedCode);

        // when
        BusinessUnitInternal result = franchiseService.create(command);

        // then
        assertNotNull(result);
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(codeGenerator, times(1)).generateFranchiseCode(Region.SEOUL);
    }

    @Test
    @DisplayName("가맹점 목록 조회")
    void getBusinessUnitList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        BusinessUnitSearchCondition condition = new BusinessUnitSearchCondition(
                null, null, null, null, null, null, null, null
        );

        List<Franchise> franchises = List.of(
                Franchise.builder().franchiseId(1L).name("가맹점1").build(),
                Franchise.builder().franchiseId(2L).name("가맹점2").build()
        );

        Page<Franchise> franchisePage = new PageImpl<>(franchises, pageable, franchises.size());
        when(franchiseRepository.search(eq(condition), eq(pageable))).thenReturn(franchisePage);

        // when
        Page<BusinessUnitInternal> result = franchiseService.getBusinessUnitList(condition, pageable);

        // then
        assertEquals(2, result.getContent().size());
        verify(franchiseRepository, times(1)).search(condition, pageable);
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
        Franchise franchise = Franchise.builder().franchiseId(id).name("기존 가맹점").phone("010-1234-5678").build();

        BusinessUnitUpdateCommand command = new BusinessUnitUpdateCommand(
                "변경된 가맹점", null, null, null, null, null,
                new BusinessUnitUpdateCommand.FranchiseUpdate(null, null, null, null, null),
                null
        );

        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.updateInfo(id, command);

        // then
        assertEquals("변경된 가맹점", franchise.getName());
        verify(franchiseRepository, times(1)).findById(id);
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
        verify(franchiseRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("가맹점 삭제")
    void delete() {

        // given
        Long id = 1L;
        Franchise franchise = spy(Franchise.builder().franchiseId(id).build());
        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.delete(id);

        // then
        verify(franchise, times(1)).delete();
    }

    @Test
    @DisplayName("가맹점 경고 부여 및 패널티")
    void addWarning() {

        // given
        Long id = 1L;
        Franchise franchise = spy(Franchise.builder().franchiseId(id).warningCount(2).build());
        when(franchiseRepository.findById(id)).thenReturn(Optional.of(franchise));

        // when
        franchiseService.addWarning(id);

        // then
        assertEquals(3, franchise.getWarningCount());
        verify(franchise, times(1)).addWarning();
    }
}