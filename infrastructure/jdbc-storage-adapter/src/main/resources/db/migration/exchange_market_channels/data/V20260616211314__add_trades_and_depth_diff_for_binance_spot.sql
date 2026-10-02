INSERT INTO exchange_market_channels (exchange_market_id, channel_id, enabled)
SELECT em.id, c.id, true
FROM exchange_markets em
         JOIN exchanges e ON e.id = em.exchange_id
         JOIN market_types mt ON mt.id = em.market_type_id
         JOIN channels c ON c.code IN ('TRADE', 'DEPTH_DIFF')
WHERE e.code = 'BINANCE'
  AND mt.code = 'SPOT';