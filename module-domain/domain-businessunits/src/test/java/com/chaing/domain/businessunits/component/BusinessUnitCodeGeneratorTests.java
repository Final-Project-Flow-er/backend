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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @SuppressWarnings("unchecked")
    @DisplayName("가맹점 코드 생성")
    void generateFranchiseCode() {

        // given
        Region region = Region.SEOUL;
        String prefix = region.getCode();
        Franchise lastFranchise = Franchise.builder().franchiseCode(prefix + "05").build();

        // when
        when(franchiseRepository.findFirstByFranchiseCodeStartingWithOrderByFranchiseCodeDesc(prefix))
                .thenReturn(Optional.empty(), Optional.of(lastFranchise));

        // then
        assertEquals(prefix + "01", codeGenerator.generateFranchiseCode(region));
        assertEquals(prefix + "06", codeGenerator.generateFranchiseCode(region));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("공장 코드 생성")
    void generateFactoryCode() {

        // given
        String prefix = "FA";
        Factory lastFactory = Factory.builder().factoryCode("FA12").build();

        // when
        when(factoryRepository.findFirstByFactoryCodeStartingWithOrderByFactoryCodeDesc(prefix))
                .thenReturn(Optional.empty(), Optional.of(lastFactory));

        // then
        assertEquals("FA01", codeGenerator.generateFactoryCode());
        assertEquals("FA13", codeGenerator.generateFactoryCode());
    }
}