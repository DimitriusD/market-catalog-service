package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.MarketTypeEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface MarketTypeRepository extends ListCrudRepository<MarketTypeEntity, Long> {

    Optional<MarketTypeEntity> findByCode(String code);
}
