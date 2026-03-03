package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.component.BusinessUnitCodeGenerator;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.repository.FactoryRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactoryServiceImplTests {

    @Mock
    private FactoryRepository factoryRepository;

    @Mock
    private BusinessUnitCodeGenerator codeGenerator;

    @InjectMocks
    private FactoryServiceImpl factoryService;

    @Test
    @DisplayName("공장 등록")
    void create() {

        // given
        String generatedCode = "FA01";
        BusinessUnitCreateCommand command = new BusinessUnitCreateCommand(
                "공장", "주소", "010-0000-0000", "대표", "0123456",
                Region.SEOUL, null, new BusinessUnitCreateCommand.FactoryCreate(5)
        );
        when(codeGenerator.generateFactoryCode()).thenReturn(generatedCode);

        // when
        BusinessUnitInternal result = factoryService.create(command);

        // then
        assertNotNull(result);
        verify(factoryRepository, times(1)).save(any(Factory.class));
        verify(codeGenerator, times(1)).generateFactoryCode();
    }

    @Test
    @DisplayName("공장 목록 조회")
    void getBusinessUnitList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Factory> factories = List.of(
                Factory.builder().factoryId(1L).build(),
                Factory.builder().factoryId(2L).build()
        );

        Page<Factory> factoryPage = new PageImpl<>(factories, pageable, factories.size());
        when(factoryRepository.findAll(pageable)).thenReturn(factoryPage);

        // when
        Page<BusinessUnitInternal> result = factoryService.getBusinessUnitList(pageable);

        // then
        assertEquals(2, result.getContent().size());
        verify(factoryRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("공장 조회")
    void getById() {

        // given
        Long id = 1L;
        Factory factory = Factory.builder().factoryId(id).name("공장").build();
        when(factoryRepository.findById(id)).thenReturn(Optional.of(factory));

        // when
        BusinessUnitInternal result = factoryService.getById(id);

        // then
        assertNotNull(result);
        assertEquals("공장", result.name());
        verify(factoryRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("공장 정보 수정")
    void updateInfo() {

        // given
        Long id = 1L;
        Factory factory = Factory.builder().factoryId(id).name("기존 이름").productionLineCount(10).build();

        BusinessUnitUpdateCommand command = new BusinessUnitUpdateCommand(
                "변경 이름", null, null, null, null,
                null, null, new BusinessUnitUpdateCommand.FactoryUpdate(null)
        );

        when(factoryRepository.findById(id)).thenReturn(Optional.of(factory));

        // when
        BusinessUnitInternal result = factoryService.updateInfo(id, command);

        // then
        assertEquals("변경 이름", factory.getName());
        assertEquals(10, factory.getProductionLineCount());
        verify(factoryRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("공장 상태 변경")
    void updateStatus() {

        // given
        Long id = 1L;
        Factory factory = spy(Factory.builder().factoryId(id).status(UsableStatus.ACTIVE).build());
        when(factoryRepository.findById(id)).thenReturn(Optional.of(factory));

        // when
        factoryService.updateStatus(id, UsableStatus.INACTIVE);

        // then
        assertEquals(UsableStatus.INACTIVE, factory.getStatus());
        verify(factory, times(1)).updateStatus(UsableStatus.INACTIVE);
    }

    @Test
    @DisplayName("공장 삭제")
    void delete() {

        // given
        Long id = 1L;
        Factory factory = spy(Factory.builder().factoryId(id).build());
        when(factoryRepository.findById(id)).thenReturn(Optional.of(factory));

        // when
        factoryService.delete(id);

        // then
        verify(factory, times(1)).delete();
    }
}