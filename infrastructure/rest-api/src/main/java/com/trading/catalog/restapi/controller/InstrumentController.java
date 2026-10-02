package com.trading.catalog.restapi.controller;

import com.trading.catalog.application.domain.model.instrument.InstrumentSearchQuery;
import com.trading.catalog.application.port.input.MarketCatalogService;
import com.trading.catalog.restapi.mapper.MarketCatalogWebMapper;
import com.trading.catalog.restapi.generated.api.InstrumentsApi;
import com.trading.catalog.restapi.generated.model.InstrumentPageWebDto;
import com.trading.catalog.restapi.generated.model.InstrumentWebDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class InstrumentController implements InstrumentsApi {

    private final MarketCatalogService marketCatalogService;
    private final MarketCatalogWebMapper marketCatalogWebMapper;

    @Override
    public InstrumentPageWebDto searchInstruments(String exchangeCode, String marketCode, String searchText,
                                                  String baseAssetCode, String quoteAssetCode,
                                                  Integer limit, String cursor) {
        final var search = new InstrumentSearchQuery(exchangeCode, marketCode, searchText, baseAssetCode, quoteAssetCode, limit, cursor);
        return marketCatalogWebMapper.toInstrumentPage(marketCatalogService.searchInstruments(search));
    }

    @Override
    public InstrumentWebDto getInstrument(String instrumentId) {
        return marketCatalogWebMapper.toInstrument(marketCatalogService.getInstrument(instrumentId));
    }
}
