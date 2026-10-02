package com.trading.catalog.application.port.input;

import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.instrument.InstrumentSearchQuery;
import com.trading.catalog.application.domain.model.market.Exchange;

import java.util.List;

public interface MarketCatalogService {

    List<Exchange> getCatalog();

    InstrumentPage searchInstruments(InstrumentSearchQuery search);

    Instrument getInstrument(String instrumentId);

    List<MarketChannel> getMarketChannels(String exchangeCode, String marketCode);
}
