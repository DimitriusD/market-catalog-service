package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.ExchangeMarketEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeMarketRepository extends ListCrudRepository<ExchangeMarketEntity, Long> {

    Optional<ExchangeMarketEntity> findByExchangeIdAndCode(Long exchangeId, String code);

    List<ExchangeMarketEntity> findByExchangeId(Long exchangeId);
}
