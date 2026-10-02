package com.trading.catalog.application.domain.model.market;

import com.trading.catalog.application.domain.model.channel.MarketChannel;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Market {
    String code;
    String marketType;
    String displayName;
    boolean enabled;
    @Singular
    List<MarketChannel> channels;
}
