package com.chaing.domain.businessunits.service.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.component.BusinessUnitCodeGenerator;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
        BusinessUnitSearchCondition condition = new BusinessUnitSearchCondition(
                null, null, null, null, null, null, null, null
        );

        List<Factory> factories = List.of(
                Factory.builder().factoryId(1L).factoryCode("FA01").name("공장1").build(),
                Factory.builder().factoryId(2L).factoryCode("FA02").name("공장2").build()
        );

        Page<Factory> factoryPage = new PageImpl<>(factories, pageable, factories.size());
        when(factoryRepository.search(eq(condition), eq(pageable))).thenReturn(factoryPage);

        // when
        Page<BusinessUnitInternal> result = factoryService.getBusinessUnitList(condition, pageable);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals("공장1", result.getContent().get(0).name());
        verify(factoryRepository, times(1)).search(condition, pageable);
    }

    @Test
    @DisplayName("이미 존재하는 이름으로 등록 시 예외 발생")
    void create_fail_duplicateName() {

        // given
        BusinessUnitCreateCommand command = new BusinessUnitCreateCommand(
                "중복공장", "주소", "010-0000-0000", "대표", "0123456",
                Region.SEOUL, null, null
        );
        when(factoryRepository.existsByNameExcludeDeleted("중복공장")).thenReturn(true);

        // when & then
        assertThrows(BusinessUnitException.class, () -> factoryService.create(command));
        verify(factoryRepository, never()).save(any());
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
        Factory factory = Factory.builder().factoryId(id).name("기존 이름").build();
        BusinessUnitUpdateCommand command = new BusinessUnitUpdateCommand(
                "새 이름", null, null, null, null, null, null,
                new BusinessUnitUpdateCommand.FactoryUpdate(15)
        );

        when(factoryRepository.findById(id)).thenReturn(Optional.of(factory));
        when(factoryRepository.existsByNameExcludeDeleted("새 이름")).thenReturn(false);

        // when
        BusinessUnitInternal result = factoryService.updateInfo(id, command);

        // then
        assertEquals("새 이름", result.name());
        verify(factoryRepository).existsByNameExcludeDeleted("새 이름");
    }

    @Test
    @DisplayName("공장 상태 변경")
    void updateStatus() {

        // given
        Long id = 1L;
        Factory factory = Factory.builder().factoryId(id).status(UsableStatus.ACTIVE).build();
        when(factoryRepository.findById(id)).thenReturn(Optional.of(factory));

        // when
        factoryService.updateStatus(id, UsableStatus.INACTIVE);

        // then
        assertEquals(UsableStatus.INACTIVE, factory.getStatus());
        verify(factoryRepository, times(1)).findById(id);
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
        verify(factoryRepository, times(1)).findById(id);
    }
}