package com.chaing.domain.returns.service;

import org.springframework.stereotype.Service;

@Service
public class FakeFranchiseService {
    public String getFranchise(Long franchiseId) {
        return "SE01";
    }
}
