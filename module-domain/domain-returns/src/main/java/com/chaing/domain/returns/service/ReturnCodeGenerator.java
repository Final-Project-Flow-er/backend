package com.chaing.domain.returns.service;

import com.chaing.core.util.CodeGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ReturnCodeGenerator implements CodeGenerator {
    @Override
    public String generate(String businessUnitId) {
        return "RESE01" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + (int)(Math.random()*100 + 1);
    }
}
