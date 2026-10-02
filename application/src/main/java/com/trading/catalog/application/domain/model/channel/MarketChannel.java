package com.trading.catalog.application.domain.model.channel;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MarketChannel {
    String code;
    String name;
    boolean enabled;
    @Singular
    List<ChannelParameter> parameters;
}
