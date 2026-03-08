package com.chaing.domain.inventories.usecase.outbound.validator;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboundValidatorImpl implements OutboundValidator{

    @Override
    public void checkPendingDataExistence(List<FactoryInventory> targets) {
        if(targets==null || targets.isEmpty()){
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
        }

        for(FactoryInventory target:targets){
            if(target.getSerialCode()==null || target.getSerialCode().isEmpty()){
                if(target.getStatus() == null) {
                    throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_INVALID);
                }
                if(target.getSerialCode() == null) {
                    throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_INVALID);
                }
                if(target.getManufactureDate() == null) {
                    throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_INVALID);
                }
            }
        }
    }

    @Override
    public void checkValidStatus(LogType status, LogType logType) {
        if (status != logType) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_OUTBOUND_STATUS);
        }
    }

    @Override
    public void checkBoxCode(String targetBoxCode) {
    if(targetBoxCode==null || targetBoxCode.isEmpty()){
        throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
    }
    }
}
