CREATE TABLE exchange_market_channel_params
(
    id                         BIGSERIAL PRIMARY KEY,
    exchange_market_channel_id BIGINT      NOT NULL,
    param_key                  VARCHAR(64) NOT NULL,
    required                   BOOLEAN     NOT NULL DEFAULT false,
    created_at                 TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exchange_market_channel_params_channel
        FOREIGN KEY (exchange_market_channel_id)
            REFERENCES exchange_market_channels (id),

    CONSTRAINT uq_exchange_market_channel_param
        UNIQUE (exchange_market_channel_id, param_key)
);