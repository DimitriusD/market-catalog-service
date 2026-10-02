package com.trading.catalog.storage.catalog;

import com.trading.catalog.application.domain.exception.ValidationException;
import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.channel.ChannelParameter;
import com.trading.catalog.application.domain.model.channel.ChannelParameterOption;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.instrument.InstrumentSearch;
import com.trading.catalog.application.domain.model.market.Exchange;
import com.trading.catalog.application.domain.model.market.Market;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs against the Flyway-seeded catalog (BINANCE: SPOT with TRADE + DEPTH_DIFF(updateSpeed),
 * OPTION without channels, 10 SPOT/USDT instruments). Each test rolls back its changes.
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CatalogStore.class)
@Testcontainers
class CatalogStoreIntegrationTest {

    private static final List<String> ALL_SPOT_SYMBOLS = List.of(
            "ADAUSDT", "AVAXUSDT", "BNBUSDT", "BTCUSDT", "DOGEUSDT",
            "ETHUSDT", "LINKUSDT", "LTCUSDT", "SOLUSDT", "XRPUSDT");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.9");

    @Autowired
    private CatalogStore store;

    @Autowired
    private JdbcTemplate jdbc;

    // ===== catalog =====

    @Test
    void catalogContainsMarketsWithCodeTypeChannelsAndParamRules() {
        List<Exchange> catalog = store.getAvailableCatalog();

        assertEquals(List.of("BINANCE"), catalog.stream().map(Exchange::getCode).toList());
        Exchange binance = catalog.getFirst();
        assertEquals(List.of("OPTION", "SPOT"), binance.getMarkets().stream().map(Market::getCode).toList());

        Market spot = market(binance, "SPOT");
        assertEquals("SPOT", spot.getMarketType());
        assertEquals("Spot", spot.getDisplayName());
        assertEquals(List.of("DEPTH_DIFF", "TRADE"), spot.getChannels().stream().map(MarketChannel::getCode).toList());

        ChannelParameter updateSpeed = channel(spot, "DEPTH_DIFF").getParameters().getFirst();
        assertEquals("updateSpeed", updateSpeed.getKey());
        assertTrue(updateSpeed.isRequired());
        assertEquals(List.of("100ms", "1000ms"), values(updateSpeed));
        assertEquals("100ms", updateSpeed.getDefaultValue());
        assertEquals("100 ms", updateSpeed.getAllowedValues().getFirst().getDisplayName());
    }

    @Test
    void marketCodeIsIndependentOfMarketType() {
        jdbc.update("""
                INSERT INTO exchange_markets (exchange_id, market_type_id, code, name)
                SELECT e.id, mt.id, 'SPOT_ISOLATED', 'Spot Isolated'
                FROM exchanges e, market_types mt
                WHERE e.code = 'BINANCE' AND mt.code = 'SPOT'
                """);

        Market isolated = market(store.getAvailableCatalog().getFirst(), "SPOT_ISOLATED");

        assertEquals("SPOT", isolated.getMarketType());
        assertEquals("Spot Isolated", isolated.getDisplayName());
    }

    @Test
    void catalogOmitsDisabledParamValuesAndTheirDefault() {
        jdbc.update("UPDATE exchange_market_channel_param_allowed_values SET enabled = false WHERE value = '100ms'");

        ChannelParameter parameter = channel(market(store.getAvailableCatalog().getFirst(), "SPOT"), "DEPTH_DIFF")
                .getParameters().getFirst();

        assertEquals(List.of("1000ms"), values(parameter));
        assertNull(parameter.getDefaultValue());
        assertEquals(parameter, store.getMarketChannels("BINANCE", "SPOT").getFirst().getParameters().getFirst());
    }

    @Test
    void catalogOmitsDisabledChannelMarketAndExchange() {
        jdbc.update("""
                UPDATE exchange_market_channels SET enabled = false
                WHERE channel_id = (SELECT id FROM channels WHERE code = 'TRADE')
                """);
        assertEquals(List.of("DEPTH_DIFF"), market(store.getAvailableCatalog().getFirst(), "SPOT")
                .getChannels().stream().map(MarketChannel::getCode).toList());

        jdbc.update("UPDATE exchange_markets SET enabled = false WHERE code = 'OPTION'");
        assertEquals(List.of("SPOT"), store.getAvailableCatalog().getFirst()
                .getMarkets().stream().map(Market::getCode).toList());

        jdbc.update("UPDATE exchanges SET enabled = false WHERE code = 'BINANCE'");
        assertTrue(store.getAvailableCatalog().isEmpty());
    }

    // ===== instrument search =====

    @Test
    void searchReturnsEnabledInstrumentsOrderedBySymbol() {
        InstrumentPage page = store.searchInstruments(search(null, null, null, 200, null)).orElseThrow();

        assertEquals(ALL_SPOT_SYMBOLS, symbols(page));
        assertNull(page.nextCursor());
        Instrument btc = page.items().get(3);
        assertEquals("BINANCE|SPOT|BTC|USDT", btc.getInstrumentId());
        assertEquals("BINANCE", btc.getExchangeCode());
        assertEquals("SPOT", btc.getMarketCode());
        assertEquals("Bitcoin", btc.getBaseAsset().getName());
        assertEquals("USDT", btc.getQuoteAsset().getCode());
        assertEquals("BTC/USDT", btc.getDisplaySymbol());
    }

    @Test
    void searchPagesThroughAllInstrumentsWithCursor() {
        List<String> collected = new ArrayList<>();
        String cursor = null;
        int pages = 0;
        do {
            InstrumentPage page = store.searchInstruments(search(null, null, null, 3, cursor)).orElseThrow();
            collected.addAll(symbols(page));
            cursor = page.nextCursor();
            pages++;
        } while (cursor != null);

        assertEquals(ALL_SPOT_SYMBOLS, collected);
        assertEquals(4, pages);
    }

    @Test
    void lastFullPageHasNoCursor() {
        InstrumentPage page = store.searchInstruments(search(null, null, null, 10, null)).orElseThrow();

        assertEquals(10, page.items().size());
        assertNull(page.nextCursor());
    }

    @Test
    void searchMatchesSymbolDisplaySymbolAndBaseAssetName() {
        assertEquals(List.of("BTCUSDT"), symbols(store.searchInstruments(search("bitcoin", null, null, 50, null)).orElseThrow()));
        assertEquals(List.of("ETHUSDT"), symbols(store.searchInstruments(search("eth/", null, null, 50, null)).orElseThrow()));
        assertEquals(List.of("LINKUSDT", "LTCUSDT"), symbols(store.searchInstruments(search("li", null, null, 50, null)).orElseThrow()));
    }

    @Test
    void searchTreatsLikeWildcardsLiterally() {
        assertTrue(store.searchInstruments(search("%", null, null, 50, null)).orElseThrow().items().isEmpty());
        assertTrue(store.searchInstruments(search("_", null, null, 50, null)).orElseThrow().items().isEmpty());
    }

    @Test
    void searchFiltersByAssets() {
        assertEquals(List.of("SOLUSDT"), symbols(store.searchInstruments(search(null, "SOL", "USDT", 50, null)).orElseThrow()));
        assertTrue(store.searchInstruments(search(null, null, "USDC", 50, null)).orElseThrow().items().isEmpty());
    }

    @Test
    void searchAllowsSeveralContractsForTheSamePair() {
        jdbc.update("""
                INSERT INTO instruments (instrument_id, exchange_market_id, base_asset_id, quote_asset_id,
                                         exchange_symbol, display_symbol)
                SELECT 'BINANCE|SPOT|BTC|USDT|ALT', i.exchange_market_id, i.base_asset_id, i.quote_asset_id,
                       'BTCUSDT_ALT', 'BTC/USDT alt'
                FROM instruments i WHERE i.instrument_id = 'BINANCE|SPOT|BTC|USDT'
                """);

        assertEquals(List.of("BTCUSDT", "BTCUSDT_ALT"),
                symbols(store.searchInstruments(search(null, "BTC", "USDT", 50, null)).orElseThrow()));
    }

    @Test
    void searchSkipsDisabledInstruments() {
        jdbc.update("UPDATE instruments SET enabled = false WHERE exchange_symbol = 'BTCUSDT'");

        assertFalse(symbols(store.searchInstruments(search(null, null, null, 50, null)).orElseThrow()).contains("BTCUSDT"));
    }

    @Test
    void searchIsEmptyForUnknownOrUnavailableMarket() {
        assertTrue(store.searchInstruments(new InstrumentSearch("BINANCE", "FUTURES", null, null, null, null, null)).isEmpty());

        jdbc.update("UPDATE exchange_markets SET enabled = false WHERE code = 'SPOT'");
        assertTrue(store.searchInstruments(search(null, null, null, 50, null)).isEmpty());

        jdbc.update("UPDATE exchange_markets SET enabled = true WHERE code = 'SPOT'");
        jdbc.update("UPDATE exchanges SET enabled = false WHERE code = 'BINANCE'");
        assertTrue(store.searchInstruments(search(null, null, null, 50, null)).isEmpty());
    }

    @Test
    void searchRejectsMalformedCursor() {
        assertThrows(ValidationException.class,
                () -> store.searchInstruments(search(null, null, null, 50, "not-a-cursor")));
    }

    // ===== single instrument and channels =====

    @Test
    void instrumentIsUnavailableWhenItsMarketIsDisabled() {
        assertTrue(store.findInstrumentByInstrumentId("BINANCE|SPOT|BTC|USDT").orElseThrow().isEnabled());

        jdbc.update("UPDATE exchange_markets SET enabled = false WHERE code = 'SPOT'");

        Instrument btc = store.findInstrumentByInstrumentId("BINANCE|SPOT|BTC|USDT").orElseThrow();
        assertFalse(btc.isEnabled());
        assertEquals("SPOT", btc.getMarketCode());
    }

    @Test
    void marketChannelsReflectAvailability() {
        List<MarketChannel> channels = store.getMarketChannels("BINANCE", "SPOT");
        assertEquals(List.of("DEPTH_DIFF", "TRADE"), channels.stream().map(MarketChannel::getCode).toList());
        assertTrue(channels.stream().allMatch(MarketChannel::isEnabled));
        assertEquals(List.of("100ms", "1000ms"), values(channels.getFirst().getParameters().getFirst()));

        jdbc.update("UPDATE exchanges SET enabled = false WHERE code = 'BINANCE'");
        assertTrue(store.getMarketChannels("BINANCE", "SPOT").stream().noneMatch(MarketChannel::isEnabled));

        assertTrue(store.getMarketChannels("BINANCE", "FUTURES").isEmpty());
    }

    private static InstrumentSearch search(String q, String base, String quote, int limit, String cursor) {
        return new InstrumentSearch("BINANCE", "SPOT", q, base, quote, limit, cursor);
    }

    private static Market market(Exchange exchange, String code) {
        return exchange.getMarkets().stream().filter(m -> m.getCode().equals(code)).findFirst().orElseThrow();
    }

    private static MarketChannel channel(Market market, String code) {
        return market.getChannels().stream().filter(c -> c.getCode().equals(code)).findFirst().orElseThrow();
    }

    private static List<String> values(ChannelParameter param) {
        return param.getAllowedValues().stream().map(ChannelParameterOption::getValue).toList();
    }

    private static List<String> symbols(InstrumentPage page) {
        return page.items().stream().map(Instrument::getExchangeSymbol).toList();
    }
}
