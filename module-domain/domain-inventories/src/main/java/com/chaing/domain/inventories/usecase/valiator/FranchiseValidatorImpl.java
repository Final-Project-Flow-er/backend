package com.chaing.domain.inventories.usecase.valiator;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.usecase.reader.Reader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Qualifier("franchise")
@RequiredArgsConstructor
public class FranchiseValidatorImpl implements Validator<FranchiseInboundCreateCommand> {

    @Qualifier("franchise")
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
    public void checkScanValidity(FranchiseInboundCreateCommand command) {

        LocalDate date = command.manufactureDate();

        // 날짜 누락 확인
        if(date == null) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_MANUFACTURED_DATE_IS_NULL);
        }

        // 날짜 유효성 확인
        if(date.isAfter(LocalDate.now())){
            throw new InventoriesException(InventoriesErrorCode.INVALID_MANUFACTURED_DATE);
        }

        List<String> serialCodes = command.serialCodes();

        // 리스트 누락 확인
        if(serialCodes == null || serialCodes.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_SERIAL_CODE_IS_NULL);
        }

        serialCodes.forEach(serialCode -> {
            // 식별 코드 누락 확인
            if (serialCode == null || serialCode.isBlank()) {
                throw new InventoriesException(InventoriesErrorCode.INVENTORIES_SERIAL_CODE_IS_NULL);
            }

            // 식별 코드 유효성 확인
            if (serialCode.length() != SERIAL_CODE_LENGTH) {
                throw new InventoriesException(InventoriesErrorCode.INVALID_SERIAL_CODE);
            }
        });
    }
}
