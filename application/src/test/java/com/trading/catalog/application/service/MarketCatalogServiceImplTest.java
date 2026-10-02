package com.trading.catalog.application.service;

import com.trading.catalog.application.domain.exception.NotFoundException;
import com.trading.catalog.application.domain.exception.ValidationException;
import com.trading.catalog.application.domain.model.Asset;
import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.instrument.InstrumentSearchQuery;
import com.trading.catalog.application.domain.model.market.Exchange;
import com.trading.catalog.application.port.output.CatalogStorePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarketCatalogServiceImplTest {

    private static final String INSTRUMENT_ID = "BINANCE|SPOT|BTC|USDT";

    private final FakeCatalogStore catalog = new FakeCatalogStore();
    private final MarketCatalogServiceImpl service = new MarketCatalogServiceImpl(catalog);

    @Test
    void resolvesCanonicalInstrumentIdIntoFullDetails() {
        catalog.instrument = Instrument.builder()
                .instrumentId(INSTRUMENT_ID)
                .exchangeCode("BINANCE")
                .marketCode("SPOT")
                .baseAsset(Asset.builder().code("BTC").name("Bitcoin").build())
                .quoteAsset(Asset.builder().code("USDT").name("Tether USD").build())
                .exchangeSymbol("BTCUSDT")
                .displaySymbol("BTC/USDT")
                .enabled(true)
                .build();

        Instrument details = service.getInstrument(INSTRUMENT_ID);

        assertEquals(INSTRUMENT_ID, details.getInstrumentId());
        assertEquals("BINANCE", details.getExchangeCode());
        assertEquals("SPOT", details.getMarketCode());
        assertEquals("BTCUSDT", details.getExchangeSymbol());
    }

    @Test
    void throwsNotFoundForUnknownInstrument() {
        assertThrows(NotFoundException.class, () -> service.getInstrument(INSTRUMENT_ID));
    }

    @Test
    void throwsValidationForBlankInstrumentId() {
        assertThrows(ValidationException.class, () -> service.getInstrument("  "));
        assertThrows(ValidationException.class, () -> service.getInstrument(null));
    }

    @Test
    void returnsInstrumentPageOfAvailableMarket() {
        catalog.page = new InstrumentPage(List.of(), null);

        InstrumentPage page = service.searchInstruments(search());

        assertSame(catalog.page, page);
    }

    @Test
    void throwsNotFoundWhenMarketIsNotAvailable() {
        var query = search();
        var ex = assertThrows(NotFoundException.class, () -> service.searchInstruments(query));
        assertEquals("Market not found: BINANCE/SPOT", ex.getMessage());
    }

    private static InstrumentSearchQuery search() {
        return new InstrumentSearchQuery("binance", "spot", null, null, null, null, null);
    }

    private static final class FakeCatalogStore implements CatalogStorePort {
        Instrument instrument;
        InstrumentPage page;

        @Override
        public List<Exchange> getAvailableCatalog() {
            return List.of();
        }

        @Override
        public Optional<InstrumentPage> searchInstruments(InstrumentSearchQuery search) {
            return Optional.ofNullable(page);
        }

        @Override
        public Optional<Instrument> findInstrumentByInstrumentId(String instrumentId) {
            return Optional.ofNullable(instrument)
                    .filter(i -> i.getInstrumentId().equals(instrumentId));
        }

        @Override
        public List<MarketChannel> getMarketChannels(String exchangeCode, String marketCode) {
            return List.of();
        }
    }
}
