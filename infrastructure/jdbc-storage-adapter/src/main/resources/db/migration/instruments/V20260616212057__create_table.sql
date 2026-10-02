CREATE TABLE instruments
(
    id                 BIGSERIAL PRIMARY KEY,

    instrument_id      VARCHAR(128) NOT NULL UNIQUE,

    exchange_market_id BIGINT       NOT NULL,

    base_asset_id      BIGINT       NOT NULL,
    quote_asset_id     BIGINT       NOT NULL,

    exchange_symbol    VARCHAR(64)  NOT NULL,
    display_symbol     VARCHAR(64)  NOT NULL,

    name               VARCHAR(128),
    description        TEXT,

    enabled            BOOLEAN      NOT NULL DEFAULT true,

    price_precision    INTEGER,
    quantity_precision INTEGER,

    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_instruments_exchange_market
        FOREIGN KEY (exchange_market_id)
            REFERENCES exchange_markets (id),

    CONSTRAINT fk_instruments_base_asset
        FOREIGN KEY (base_asset_id)
            REFERENCES assets (id),

    CONSTRAINT fk_instruments_quote_asset
        FOREIGN KEY (quote_asset_id)
            REFERENCES assets (id),

    CONSTRAINT uq_instrument_symbol
        UNIQUE (exchange_market_id, exchange_symbol),

    CONSTRAINT uq_instrument_pair
        UNIQUE (exchange_market_id, base_asset_id, quote_asset_id)
);