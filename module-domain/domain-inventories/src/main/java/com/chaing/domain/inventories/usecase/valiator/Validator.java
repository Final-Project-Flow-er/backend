package com.chaing.domain.inventories.usecase.valiator;

import jakarta.validation.constraints.NotBlank;

public interface Validator<T> {

    void checkAlreadyScanned(@NotBlank String serialCode);

    void checkScanValidity(T command);

}
