package com.trading.catalog.config;

import com.trading.catalog.application.port.input.MarketCatalogService;
import com.trading.catalog.application.port.output.CatalogStorePort;
import com.trading.catalog.application.service.MarketCatalogServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationServiceConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public MarketCatalogService marketCatalogService(CatalogStorePort catalogStorePort) {
        return new MarketCatalogServiceImpl(catalogStorePort);
    }
}
