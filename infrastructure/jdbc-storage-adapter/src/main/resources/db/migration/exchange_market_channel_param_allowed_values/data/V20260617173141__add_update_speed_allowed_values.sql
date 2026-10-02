INSERT INTO exchange_market_channel_param_allowed_values (channel_param_id,
                                                          value,
                                                          display_name,
                                                          sort_order,
                                                          is_default,
                                                          enabled)
SELECT p.id,
       v.value,
       v.display_name,
       v.sort_order,
       v.is_default,
       true
FROM exchange_market_channel_params p
         JOIN exchange_market_channels emc ON emc.id = p.exchange_market_channel_id
         JOIN exchange_markets em ON em.id = emc.exchange_market_id
         JOIN exchanges e ON e.id = em.exchange_id
         JOIN market_types mt ON mt.id = em.market_type_id
         JOIN channels c ON c.id = emc.channel_id
         CROSS JOIN (VALUES ('100ms', '100 ms', 1, true),
                            ('1000ms', '1000 ms', 2, false)) AS v(value, display_name, sort_order, is_default)
WHERE e.code = 'BINANCE'
  AND mt.code = 'SPOT'
  AND c.code = 'DEPTH_DIFF'
  AND p.param_key = 'updateSpeed'
ON CONFLICT (channel_param_id, value) DO UPDATE SET display_name = EXCLUDED.display_name,
                                                    sort_order   = EXCLUDED.sort_order,
                                                    is_default   = EXCLUDED.is_default,
                                                    enabled      = EXCLUDED.enabled,
                                                    updated_at   = CURRENT_TIMESTAMP;
