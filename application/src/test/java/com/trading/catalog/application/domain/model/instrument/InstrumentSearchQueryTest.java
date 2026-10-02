package com.trading.catalog.application.domain.model.instrument;

import com.trading.catalog.application.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstrumentSearchQueryTest {

    @Test
    void normalizesCodesQueryAndDefaultLimit() {
        var search = new InstrumentSearchQuery(" binance ", "spot", "  BTC ", "btc", null, null, null);

        assertEquals("BINANCE", search.exchangeCode());
        assertEquals("SPOT", search.marketCode());
        assertEquals("btc", search.searchText());
        assertEquals("BTC", search.baseAssetCode());
        assertNull(search.quoteAssetCode());
        assertEquals(50, search.limit());
    }

    @Test
    void treatsMissingQueryAsEmpty() {
        assertEquals("", new InstrumentSearchQuery("BINANCE", "SPOT", null, null, null, 10, null).searchText());
    }

    @Test
    void rejectsMissingMarket() {
        assertThrows(ValidationException.class,
                () -> new InstrumentSearchQuery("BINANCE", " ", null, null, null, null, null));
        assertThrows(ValidationException.class,
                () -> new InstrumentSearchQuery(null, "SPOT", null, null, null, null, null));
    }

    @Test
    void rejectsLimitOutOfRange() {
        assertThrows(ValidationException.class,
                () -> new InstrumentSearchQuery("BINANCE", "SPOT", null, null, null, 0, null));
        assertThrows(ValidationException.class,
                () -> new InstrumentSearchQuery("BINANCE", "SPOT", null, null, null, 201, null));
    }

    @Test
    void rejectsBlankCursor() {
        assertThrows(ValidationException.class,
                () -> new InstrumentSearchQuery("BINANCE", "SPOT", null, null, null, null, " "));
    }
}
