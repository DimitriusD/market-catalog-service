package com.trading.catalog.application.domain.model.market;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Exchange {
    String code;
    String displayName;
    boolean enabled;
    @Singular
    List<Market> markets;
}
