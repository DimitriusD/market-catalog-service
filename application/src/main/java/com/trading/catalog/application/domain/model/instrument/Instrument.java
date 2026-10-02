package com.trading.catalog.application.domain.model.instrument;

import com.trading.catalog.application.domain.model.Asset;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Instrument {
    String instrumentId;
    String exchangeCode;
    String marketCode;
    Asset baseAsset;
    Asset quoteAsset;
    String exchangeSymbol;
    String displaySymbol;
    boolean enabled;
}
