package com.trading.catalog.restapi.controller;

import com.trading.catalog.application.port.input.MarketCatalogService;
import com.trading.catalog.restapi.mapper.MarketCatalogWebMapper;
import com.trading.catalog.restapi.generated.api.CatalogApi;
import com.trading.catalog.restapi.generated.api.MarketsApi;
import com.trading.catalog.restapi.generated.model.CatalogWebDto;
import com.trading.catalog.restapi.generated.model.ChannelCapabilityWebDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class MarketCatalogController implements CatalogApi, MarketsApi {

    private final MarketCatalogService marketCatalogService;
    private final MarketCatalogWebMapper marketCatalogWebMapper;

    @Override
    public CatalogWebDto getCatalog() {
        return marketCatalogWebMapper.toCatalog(marketCatalogService.getCatalog());
    }

    @Override
    public List<ChannelCapabilityWebDto> getChannelCapabilities(String exchangeCode, String marketCode) {
        return marketCatalogWebMapper.toChannelCapabilities(
                marketCatalogService.getMarketChannels(exchangeCode, marketCode));
    }
}
