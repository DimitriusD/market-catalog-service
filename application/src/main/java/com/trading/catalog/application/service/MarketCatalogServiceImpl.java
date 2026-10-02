package com.trading.catalog.application.service;

import com.trading.catalog.application.domain.exception.NotFoundException;
import com.trading.catalog.application.domain.exception.ValidationException;
import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.instrument.InstrumentSearchQuery;
import com.trading.catalog.application.domain.model.market.Exchange;
import com.trading.catalog.application.port.input.MarketCatalogService;
import com.trading.catalog.application.port.output.CatalogStorePort;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MarketCatalogServiceImpl implements MarketCatalogService {

    private final CatalogStorePort catalogStore;

    @Override
    public List<Exchange> getCatalog() {
        return catalogStore.getAvailableCatalog();
    }

    @Override
    public InstrumentPage searchInstruments(InstrumentSearchQuery search) {
        return catalogStore.searchInstruments(search)
                .orElseThrow(() -> new NotFoundException(
                        "Market not found: " + search.exchangeCode() + "/" + search.marketCode()));
    }

    @Override
    public Instrument getInstrument(String instrumentId) {
        if (instrumentId == null || instrumentId.isBlank()) {
            throw new ValidationException("instrumentId must not be blank");
        }
        return catalogStore.findInstrumentByInstrumentId(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found: " + instrumentId));
    }

    @Override
    public List<MarketChannel> getMarketChannels(String exchangeCode, String marketCode) {
        return catalogStore.getMarketChannels(exchangeCode, marketCode);
    }
}
