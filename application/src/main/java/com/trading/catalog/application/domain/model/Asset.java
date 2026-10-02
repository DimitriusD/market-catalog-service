package com.trading.catalog.application.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Asset {
    String code;
    String name;
}
