package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.ExchangeMarketChannelEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeMarketChannelRepository extends ListCrudRepository<ExchangeMarketChannelEntity, Long> {

    Optional<ExchangeMarketChannelEntity> findByExchangeMarketIdAndChannelId(Long exchangeMarketId, Long channelId);

    List<ExchangeMarketChannelEntity> findByExchangeMarketId(Long exchangeMarketId);
}
