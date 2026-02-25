package com.chaing.domain.returns.service;

import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {
    public List<ReturnToInventoryRequest> getProducts(List<String> serialCodes) {
        return List.of(
                new ReturnToInventoryRequest(
                        "OR0101",
                        1L,
                        "BoxCode"
                )
        );
    }

    public List<Long> getProductsBySerialCodeAndBoxCode(List<FranchiseReturnUpdateRequest> requests) {
        return List.of(1L, 2L);
    }
}
