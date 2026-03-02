package com.chaing.domain.businessunits.component;

import com.chaing.core.enums.Region;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.repository.FactoryRepository;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessUnitCodeGenerator {

    private final FranchiseRepository franchiseRepository;
    private final FactoryRepository factoryRepository;

    // 가맹점 코드 생성
    public String generateFranchiseCode(Region region) {
        String prefix = region.getCode();
        return franchiseRepository.findFirstByFranchiseCodeStartingWithOrderByFranchiseCodeDesc(prefix)
                .map(franchise -> {
                    String lastFullCode = franchise.getFranchiseCode();
                    int nextNum = Integer.parseInt(lastFullCode.substring(prefix.length())) + 1;

                    if (nextNum > 99) {
                        throw new BusinessUnitException(BusinessUnitErrorCode.CODE_OVERFLOW);
                    }

                    return String.format("%s%02d", prefix, nextNum);
                })
                .orElse(prefix + "01");
    }

    // 공장 코드 생성
    public String generateFactoryCode() {
        String prefix = "FA";
        return factoryRepository.findFirstByFactoryCodeStartingWithOrderByFactoryCodeDesc(prefix)
                .map(factory -> {
                    String lastFullCode = factory.getFactoryCode();
                    int nextNum = Integer.parseInt(lastFullCode.substring(prefix.length())) + 1;

                    if (nextNum > 99) {
                        throw new BusinessUnitException(BusinessUnitErrorCode.CODE_OVERFLOW);
                    }

                    return String.format("%s%02d", prefix, nextNum);
                })
                .orElse(prefix + "01");
    }
}
