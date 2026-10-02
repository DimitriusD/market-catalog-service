package com.trading.catalog.application.domain.model.channel;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ChannelParameter {
    String key;
    boolean required;
    String defaultValue;
    @Singular
    List<ChannelParameterOption> allowedValues;
}
