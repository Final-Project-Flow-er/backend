package com.chaing.domain.sales.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SalesCodeGenerator implements CodeGenerator {
    @Override
    public String generate() {
        return LocalDateTime.now() + String.valueOf(Math.random()*100 + 1);
    }
}
