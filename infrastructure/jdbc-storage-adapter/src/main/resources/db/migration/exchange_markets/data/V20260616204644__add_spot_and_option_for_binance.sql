INSERT INTO exchange_markets (exchange_id, market_type_id, enabled)
SELECT e.id, mt.id, TRUE
FROM exchanges e
         JOIN market_types mt ON mt.code IN ('SPOT', 'OPTION')
WHERE e.code = 'BINANCE';
