INSERT INTO exchange_market_channel_params (exchange_market_channel_id, param_key, required)
SELECT emc.id, 'updateSpeed', true
FROM exchange_market_channels emc
         JOIN exchange_markets em ON em.id = emc.exchange_market_id
         JOIN exchanges e ON e.id = em.exchange_id
         JOIN market_types mt ON mt.id = em.market_type_id
         JOIN channels c ON c.id = emc.channel_id
WHERE e.code = 'BINANCE'
  AND mt.code = 'SPOT'
  AND c.code = 'DEPTH_DIFF';
