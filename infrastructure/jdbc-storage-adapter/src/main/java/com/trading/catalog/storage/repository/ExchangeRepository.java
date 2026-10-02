package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.ExchangeEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ExchangeRepository extends ListCrudRepository<ExchangeEntity, Long> {

    Optional<ExchangeEntity> findByCode(String code);
}
