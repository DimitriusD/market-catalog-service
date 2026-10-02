package com.trading.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Full stack over HTTP against the Flyway-seeded catalog; read-only, so tests share one database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class CatalogApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.9");

    @Autowired
    private TestRestTemplate rest;

    @Test
    void catalogReturnsMarketsWithChannelsAndParamRules() {
        JsonNode catalog = get("/api/v1/catalog", HttpStatus.OK);

        JsonNode binance = catalog.at("/exchanges/0");
        assertEquals("BINANCE", binance.get("code").asString());
        assertEquals("Binance", binance.get("displayName").asString());
        JsonNode spot = binance.at("/markets/1");
        assertEquals("SPOT", spot.get("code").asString());
        assertEquals("SPOT", spot.get("marketType").asString());

        JsonNode updateSpeed = spot.at("/channels/0/params/0");
        assertEquals("DEPTH_DIFF", spot.at("/channels/0/code").asString());
        assertEquals("updateSpeed", updateSpeed.get("key").asString());
        assertTrue(updateSpeed.get("required").asBoolean());
        assertEquals("100ms", updateSpeed.get("defaultValue").asString());
        assertEquals("1000 ms", updateSpeed.at("/values/1/displayName").asString());
        assertTrue(binance.at("/markets/1/channels/1/params").isEmpty(), "TRADE has no params");
    }

    @Test
    void instrumentsArePagedWithCursor() {
        List<String> symbols = new ArrayList<>();
        String url = "/api/v1/instruments?exchangeCode=binance&marketCode=spot&limit=4";
        String cursor = null;
        do {
            JsonNode page = get(cursor == null ? url : url + "&cursor=" + cursor, HttpStatus.OK);
            page.get("items").forEach(item -> symbols.add(item.get("exchangeSymbol").asString()));
            cursor = page.path("nextCursor").isString() ? page.get("nextCursor").asString() : null;
        } while (cursor != null);

        assertEquals(List.of("ADAUSDT", "AVAXUSDT", "BNBUSDT", "BTCUSDT", "DOGEUSDT",
                "ETHUSDT", "LINKUSDT", "LTCUSDT", "SOLUSDT", "XRPUSDT"), symbols);
    }

    @Test
    void instrumentSearchReturnsSelectionFields() {
        JsonNode page = get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=SPOT&q=BTC", HttpStatus.OK);

        JsonNode btc = page.at("/items/0");
        assertEquals(1, page.get("items").size());
        assertEquals("BINANCE|SPOT|BTC|USDT", btc.get("instrumentId").asString());
        assertEquals("BTCUSDT", btc.get("exchangeSymbol").asString());
        assertEquals("BTC/USDT", btc.get("displaySymbol").asString());
        assertEquals("Bitcoin", btc.at("/baseAsset/name").asString());
        assertEquals("USDT", btc.at("/quoteAsset/code").asString());
        assertTrue(page.get("nextCursor").isNull());
    }

    @Test
    void instrumentSearchFiltersByAssets() {
        JsonNode page = get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=SPOT&baseAssetCode=eth&quoteAssetCode=usdt",
                HttpStatus.OK);

        assertEquals(1, page.get("items").size());
        assertEquals("ETHUSDT", page.at("/items/0/exchangeSymbol").asString());
    }

    @Test
    void instrumentSearchRejectsInvalidRequests() {
        get("/api/v1/instruments?exchangeCode=BINANCE", HttpStatus.BAD_REQUEST);
        get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=SPOT&limit=0", HttpStatus.BAD_REQUEST);
        get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=SPOT&limit=201", HttpStatus.BAD_REQUEST);
        get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=SPOT&limit=abc", HttpStatus.BAD_REQUEST);
        JsonNode error = get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=SPOT&cursor=garbage", HttpStatus.BAD_REQUEST);
        assertEquals("Invalid cursor", error.get("message").asString());
    }

    @Test
    void instrumentSearchReturns404ForUnknownMarket() {
        JsonNode error = get("/api/v1/instruments?exchangeCode=BINANCE&marketCode=FUTURES", HttpStatus.NOT_FOUND);

        assertEquals("Market not found: BINANCE/FUTURES", error.get("message").asString());
    }

    @Test
    void instrumentIsResolvedByEncodedId() {
        ResponseEntity<JsonNode> response =
                rest.getForEntity("/api/v1/instruments/{id}", JsonNode.class, "BINANCE|SPOT|BTC|USDT");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("BTCUSDT", response.getBody().get("exchangeSymbol").asString());
        assertTrue(response.getBody().get("enabled").asBoolean());

        assertEquals(HttpStatus.NOT_FOUND,
                rest.getForEntity("/api/v1/instruments/{id}", JsonNode.class, "BINANCE|SPOT|XXX|USDT").getStatusCode());
    }

    @Test
    void channelCapabilitiesAreServedForControl() {
        JsonNode capabilities = get("/api/v1/markets/BINANCE/SPOT/channel-capabilities", HttpStatus.OK);

        assertEquals("DEPTH_DIFF", capabilities.at("/0/code").asString());
        assertEquals("100ms", capabilities.at("/0/params/0/allowedValues/0").asString());
        assertTrue(get("/api/v1/markets/BINANCE/FUTURES/channel-capabilities", HttpStatus.OK).isEmpty());
    }

    @Test
    void removedEndpointsAnswer404() {
        get("/api/v1/markets", HttpStatus.NOT_FOUND);
        get("/api/v1/markets/BINANCE/SPOT/instruments", HttpStatus.NOT_FOUND);
    }

    private JsonNode get(String url, HttpStatus expectedStatus) {
        ResponseEntity<JsonNode> response = rest.getForEntity(url, JsonNode.class);
        assertEquals(expectedStatus, response.getStatusCode(), () -> url + " -> " + response.getBody());
        return response.getBody();
    }
}
