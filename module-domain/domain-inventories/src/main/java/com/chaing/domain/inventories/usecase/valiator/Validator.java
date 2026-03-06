package com.chaing.domain.inventories.usecase.valiator;

import com.chaing.domain.inventories.entity.FranchiseInventory;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface Validator<T, R> {

    void checkAlreadyScanned(@NotBlank String serialCode);

    void checkScanValidity(T command);

    void checkPendingDataExistence(List<R> entities);
}
