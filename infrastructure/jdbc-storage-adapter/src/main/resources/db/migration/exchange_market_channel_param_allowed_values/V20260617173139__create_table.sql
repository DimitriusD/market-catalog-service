CREATE TABLE exchange_market_channel_param_allowed_values
(
    id               BIGSERIAL PRIMARY KEY,
    channel_param_id BIGINT       NOT NULL,
    value            VARCHAR(128) NOT NULL,
    display_name     VARCHAR(128),
    sort_order       INTEGER      NOT NULL DEFAULT 0,
    is_default       BOOLEAN      NOT NULL DEFAULT false,
    enabled          BOOLEAN      NOT NULL DEFAULT true,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_channel_param_allowed_values_param
        FOREIGN KEY (channel_param_id)
            REFERENCES exchange_market_channel_params (id),

    CONSTRAINT uq_channel_param_allowed_value
        UNIQUE (channel_param_id, value)
);
