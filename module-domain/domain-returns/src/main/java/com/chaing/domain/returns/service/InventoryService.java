package com.chaing.domain.returns.service;

import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
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
}
