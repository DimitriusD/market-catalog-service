package com.trading.catalog.storage.repository;

import com.trading.catalog.storage.entity.ChannelParameterOptionEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ChannelParameterOptionRepository
        extends ListCrudRepository<ChannelParameterOptionEntity, Long> {

    List<ChannelParameterOptionEntity> findByChannelParamId(Long channelParamId);
}
