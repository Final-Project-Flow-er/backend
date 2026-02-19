package com.chaing.domain.sales.service;

import com.chaing.domain.sales.repository.FranchiseSalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FranchiseSalesService {

    private final FranchiseSalesRepository franchiseSalesRepository;
}
