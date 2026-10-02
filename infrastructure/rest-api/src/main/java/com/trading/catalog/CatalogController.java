package com.trading.catalog;

import com.trading.catalog.application.port.input.MarketCatalogService;
import com.trading.catalog.mapper.MarketCatalogWebMapper;
import com.trading.catalog.restapi.generated.api.CatalogApi;
import com.trading.catalog.restapi.generated.model.CatalogWebDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CatalogController implements CatalogApi {

    private final MarketCatalogService marketCatalogService;
    private final MarketCatalogWebMapper marketCatalogWebMapper;

    @Override
    public CatalogWebDto getCatalog() {
        return marketCatalogWebMapper.toCatalog(marketCatalogService.getCatalog());
    }
}
