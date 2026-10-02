-- Preserve existing market IDs, instrument IDs and all foreign keys.
ALTER TABLE exchange_markets ADD COLUMN code VARCHAR(32);
ALTER TABLE exchange_markets ADD COLUMN name VARCHAR(128);

UPDATE exchange_markets em
SET code = mt.code, name = mt.name
FROM market_types mt
WHERE mt.id = em.market_type_id;

ALTER TABLE exchange_markets ALTER COLUMN code SET NOT NULL;
ALTER TABLE exchange_markets ALTER COLUMN name SET NOT NULL;
ALTER TABLE exchange_markets DROP CONSTRAINT uq_exchange_markets;
ALTER TABLE exchange_markets ADD CONSTRAINT uq_exchange_market_code UNIQUE (exchange_id, code);

-- Several contracts may share the same assets within a market.
ALTER TABLE instruments DROP CONSTRAINT uq_instrument_pair;

CREATE INDEX idx_instruments_available_market_symbol
    ON instruments (exchange_market_id, exchange_symbol COLLATE "C", id) WHERE enabled;
CREATE INDEX idx_instruments_available_market_assets
    ON instruments (exchange_market_id, base_asset_id, quote_asset_id) WHERE enabled;
