CREATE TABLE exchange_market_channels
(
    id                 BIGSERIAL PRIMARY KEY,

    exchange_market_id BIGINT    NOT NULL,
    channel_id         BIGINT    NOT NULL,

    enabled            BOOLEAN   NOT NULL DEFAULT true,

    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exchange_market_channels_exchange_market
        FOREIGN KEY (exchange_market_id)
            REFERENCES exchange_markets (id),

    CONSTRAINT fk_exchange_market_channels_channel
        FOREIGN KEY (channel_id)
            REFERENCES channels (id),

    CONSTRAINT uq_exchange_market_channel
        UNIQUE (exchange_market_id, channel_id)
);