package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.ChannelTypeEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ChannelTypeRepository extends ListCrudRepository<ChannelTypeEntity, Long> {

    Optional<ChannelTypeEntity> findByCode(String code);
}
