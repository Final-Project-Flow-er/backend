package com.chaing.domain.orders.service;

import com.chaing.core.util.CodeGenerator;
import com.chaing.core.util.RedisSequenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class HQOrderCodeGenerator implements CodeGenerator {

    private final RedisSequenceGenerator redisSequenceGenerator;

    @Override
    public String generate(String businessUnitId) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String seq = redisSequenceGenerator.nextSequence("hq-order");
        return "HEAD" + today + seq;
    }
}