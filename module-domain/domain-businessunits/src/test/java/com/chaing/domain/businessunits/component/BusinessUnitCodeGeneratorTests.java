package com.chaing.domain.businessunits.component;

import com.chaing.core.enums.Region;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.repository.FactoryRepository;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitCodeGeneratorTests {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private FactoryRepository factoryRepository;

    @InjectMocks
    private BusinessUnitCodeGenerator codeGenerator;

    @Test
    @DisplayName("가맹점 코드 생성")
    void generateFranchiseCode() {

        // given
        Region region = Region.SEOUL;
        String prefix = region.getCode();
        Franchise lastFranchise = Franchise.builder().franchiseCode(prefix + "05").build();

        // when
        when(franchiseRepository.findMaxCodeByPrefix(eq(prefix), any(Pageable.class)))
                .thenReturn(Collections.emptyList(), List.of(lastFranchise));

        // then
        assertEquals(prefix + "01", codeGenerator.generateFranchiseCode(region));
        assertEquals(prefix + "06", codeGenerator.generateFranchiseCode(region));
    }

    @Test
    @DisplayName("공장 코드 생성")
    void generateFactoryCode() {

        // given
        String prefix = "FA";
        Factory lastFactory = Factory.builder().factoryCode("FA12").build();

        // when
        when(factoryRepository.findMaxCodeByPrefix(eq(prefix), any(Pageable.class)))
                .thenReturn(Collections.emptyList(), List.of(lastFactory));

        // then
        assertEquals("FA01", codeGenerator.generateFactoryCode());
        assertEquals("FA13", codeGenerator.generateFactoryCode());
    }
}