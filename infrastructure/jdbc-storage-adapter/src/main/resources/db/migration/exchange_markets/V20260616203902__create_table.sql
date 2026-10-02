CREATE TABLE exchange_markets
(
    id             BIGSERIAL PRIMARY KEY,
    exchange_id    BIGINT    NOT NULL,
    market_type_id BIGINT    NOT NULL,
    enabled        BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_exchange_markets UNIQUE (exchange_id, market_type_id),

    CONSTRAINT fk_exchange_markets_exchange
        FOREIGN KEY (exchange_id)
            REFERENCES exchanges (id),

    CONSTRAINT fk_exchange_markets_market_type
        FOREIGN KEY (market_type_id)
            REFERENCES market_types (id)
);
