package com.trading.catalog.restapi.mapper;

import com.trading.catalog.application.domain.model.Asset;
import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.channel.ChannelParameter;
import com.trading.catalog.application.domain.model.channel.ChannelParameterOption;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.market.Exchange;
import com.trading.catalog.application.domain.model.market.Market;
import com.trading.catalog.restapi.generated.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarketCatalogWebMapper {

    default CatalogWebDto toCatalog(List<Exchange> exchanges) {
        return new CatalogWebDto().exchanges(toCatalogExchanges(exchanges));
    }

    List<CatalogExchangeWebDto> toCatalogExchanges(List<Exchange> exchanges);

    CatalogExchangeWebDto toCatalogExchange(Exchange exchange);

    CatalogMarketWebDto toCatalogMarket(Market market);

    @Mapping(target = "params", source = "parameters")
    CatalogChannelWebDto toCatalogChannel(MarketChannel channel);

    @Mapping(target = "values", source = "allowedValues")
    CatalogChannelParamWebDto toCatalogChannelParam(ChannelParameter parameter);

    CatalogParamValueWebDto toCatalogParamValue(ChannelParameterOption option);

    InstrumentPageWebDto toInstrumentPage(InstrumentPage page);

    default JsonNullable<String> nullable(String value) {
        return JsonNullable.of(value);
    }

    InstrumentWebDto toInstrument(Instrument instrument);

    AssetWebDto toAsset(Asset asset);

    List<ChannelCapabilityWebDto> toChannelCapabilities(List<MarketChannel> channels);

    @Mapping(target = "params", source = "parameters")
    ChannelCapabilityWebDto toChannelCapability(MarketChannel channel);

    ChannelParamCapabilityWebDto toChannelParamCapability(ChannelParameter parameter);

    default String toAllowedValue(ChannelParameterOption option) {
        return option.getValue();
    }
}
