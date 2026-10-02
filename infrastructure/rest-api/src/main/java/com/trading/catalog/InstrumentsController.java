package com.trading.catalog;

import com.trading.catalog.application.domain.model.instrument.InstrumentSearch;
import com.trading.catalog.application.port.input.MarketCatalogService;
import com.trading.catalog.mapper.MarketCatalogWebMapper;
import com.trading.catalog.restapi.generated.api.InstrumentsApi;
import com.trading.catalog.restapi.generated.model.InstrumentPageWebDto;
import com.trading.catalog.restapi.generated.model.InstrumentWebDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class InstrumentsController implements InstrumentsApi {

    private final MarketCatalogService marketCatalogService;
    private final MarketCatalogWebMapper marketCatalogWebMapper;

    @Override
    public InstrumentPageWebDto searchInstruments(String exchangeCode, String marketCode, String q,
                                                  String baseAssetCode, String quoteAssetCode,
                                                  Integer limit, String cursor) {
        final var search = new InstrumentSearch(exchangeCode, marketCode, q, baseAssetCode, quoteAssetCode, limit, cursor);
        return marketCatalogWebMapper.toInstrumentPage(marketCatalogService.searchInstruments(search));
    }

    @Override
    public InstrumentWebDto getInstrument(String instrumentId) {
        return marketCatalogWebMapper.toInstrument(marketCatalogService.getInstrument(instrumentId));
    }
}
