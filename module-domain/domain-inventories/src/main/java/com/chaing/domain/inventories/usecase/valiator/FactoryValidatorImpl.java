package com.chaing.domain.inventories.usecase.valiator;

import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.usecase.reader.Reader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Qualifier("factory")
@RequiredArgsConstructor
public class FactoryValidatorImpl implements Validator<FactoryInboundCreateCommand> {

    @Qualifier("factory")
    private final Reader reader;

    public final int SERIAL_CODE_LENGTH = 10;

    @Override
    public void checkAlreadyScanned(String serialCode) {

        boolean alreadyScanned = reader.existsBySerialCode(serialCode);

        if (alreadyScanned) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_ALREADY_EXISTS);
        }
    }

    @Override
    public void checkScanValidity(FactoryInboundCreateCommand command) {

        String serialCode = command.serialCode();
        LocalDate date = command.manufactureDate();

        // 식별 코드 누락 확인
        if(serialCode == null || serialCode.isBlank()) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_SERIAL_CODE_IS_NULL);
        }

        // 식별 코드 유효성 확인
        if(serialCode.length() != SERIAL_CODE_LENGTH) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_SERIAL_CODE);
        }

        // 날짜 누락 확인
        if(date == null) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_MANUFACTURED_DATE_IS_NULL);
        }

        // 날짜 유효성 확인
        if(date.isAfter(LocalDate.now())){
            throw new InventoriesException(InventoriesErrorCode.INVALID_MANUFACTURED_DATE);
        }
    }
}
