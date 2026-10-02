package com.trading.catalog.config;

import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;

@Configuration
public class JacksonConfig {

    /** Serializes the generated {@code JsonNullable} fields of nullable contract properties as plain values. */
    @Bean
    public JacksonModule jsonNullableModule() {
        return new JsonNullableJackson3Module();
    }
}
