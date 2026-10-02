package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.ExchangeMarketChannelParamAllowedValueEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ExchangeMarketChannelParamAllowedValueRepository
        extends ListCrudRepository<ExchangeMarketChannelParamAllowedValueEntity, Long> {

    List<ExchangeMarketChannelParamAllowedValueEntity> findByChannelParamId(Long channelParamId);
}
