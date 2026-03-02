package com.chaing.domain.orders.service;

import com.chaing.core.util.CodeGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HQOrderCodeGenerator implements CodeGenerator {
    @Override
    public String generate() {
        return "HEAD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + (int)(Math.random()*100 + 1);
    }
}
