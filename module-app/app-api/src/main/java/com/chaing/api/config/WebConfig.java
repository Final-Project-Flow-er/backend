package com.chaing.api.config;

import com.chaing.domain.businessunits.enums.BusinessUnitType;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import org.springframework.lang.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, BusinessUnitType>() {
            @Override
            public BusinessUnitType convert(@Nullable String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return BusinessUnitType.valueOf(source.trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_BUSINESS_UNIT_TYPE);
                }
            }
        });
    }
}
