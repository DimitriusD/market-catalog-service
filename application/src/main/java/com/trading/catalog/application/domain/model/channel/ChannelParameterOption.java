package com.trading.catalog.application.domain.model.channel;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChannelParameterOption {
    String value;
    String displayName;
}
