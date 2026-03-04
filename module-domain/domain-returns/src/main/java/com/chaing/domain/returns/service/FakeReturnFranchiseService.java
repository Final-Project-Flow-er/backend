package com.chaing.domain.returns.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FakeReturnFranchiseService {
    public String getFranchise(Long franchiseId) {
        return "SE01";
    }

    // franchiseCode 조회
    // return: Map<returnId, franchiseCode>
    public Map<Long, String> getFranchiseCodes(Map<Long, Long> franchiseIdByReturnId) {
        Map<Long, String> franchiseCodeByReturnId = new HashMap<>();

        franchiseCodeByReturnId.put(1L, "SE01");
        return franchiseCodeByReturnId;
    }
}
