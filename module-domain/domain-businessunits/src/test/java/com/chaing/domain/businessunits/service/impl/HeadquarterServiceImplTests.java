package com.chaing.domain.businessunits.service.impl;

import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.entity.Headquarter;
import com.chaing.domain.businessunits.repository.HeadquarterRepository;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        verify(headquarterRepository).findById(id);
    }

    @Test
    @DisplayName("아이디 목록으로 이름 조회")
    void getNamesByIds() {

        // given
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "본사A"});
        mockResults.add(new Object[]{2L, "본사B"});

        when(headquarterRepository.findNamesByIds(ids)).thenReturn(mockResults);

        // when
        Map<Long, String> result = headquarterService.getNamesByIds(ids);

        // then
        assertEquals(2, result.size());
        assertEquals("본사A", result.get(1L));
        assertEquals("본사B", result.get(2L));
    }

    @Test
    @DisplayName("본사 목록 조회")
    void getBusinessUnitList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        BusinessUnitSearchCondition condition = new BusinessUnitSearchCondition(null, "삼성", null, null, null, null, null, null);

        Headquarter hq = Headquarter.builder().hqId(1L).name("삼성본사").build();
        Page<Headquarter> hqPage = new PageImpl<>(Collections.singletonList(hq));

        when(headquarterRepository.findByNameContainingIgnoreCase("삼성", pageable)).thenReturn(hqPage);

        // when
        Page<BusinessUnitInternal> result = headquarterService.getBusinessUnitList(condition, pageable);

        // then
        assertEquals(1, result.getContent().size());
        assertEquals("삼성본사", result.getContent().get(0).name());
        verify(headquarterRepository).findByNameContainingIgnoreCase("삼성", pageable);
    }

    @Test
    @DisplayName("검색 조건에 따른 모든 본사 ID 리스트 조회")
    void getAllIdsByCondition() {

        // given
        BusinessUnitSearchCondition condition = new BusinessUnitSearchCondition(
                null, "본사", null, null, null, null, null, null);
        List<Long> expectedIds = List.of(1L, 2L);
        when(headquarterRepository.findAllIdsByName("본사")).thenReturn(expectedIds);

        // when
        List<Long> result = headquarterService.getAllIdsByCondition(condition);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        verify(headquarterRepository, times(1)).findAllIdsByName("본사");
    }

    @Test
    @DisplayName("본사 정보 수정")
    void updateInfo() {

        // given
        Long id = 1L;
        Headquarter hq = Headquarter.builder().hqId(id).name("기존").build();
        BusinessUnitUpdateCommand command = new BusinessUnitUpdateCommand("변경", null, null, null, null, null, null, null);

        when(headquarterRepository.findById(id)).thenReturn(Optional.of(hq));

        // when
        BusinessUnitInternal result = headquarterService.updateInfo(id, command);

        // then
        assertEquals("변경", result.name());
    }

    @Test
    @DisplayName("본사 코드 조회")
    void getHqCode() {

        // given
        Long id = 1L;
        Headquarter hq = Headquarter.builder().hqId(id).hqCode("HQ001").build();
        when(headquarterRepository.findById(id)).thenReturn(Optional.of(hq));

        // when
        String code = headquarterService.getHqCode(id);

        // then
        assertEquals("HQ001", code);
    }
}