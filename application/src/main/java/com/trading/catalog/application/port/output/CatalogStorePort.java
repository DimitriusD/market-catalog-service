package com.trading.catalog.application.port.output;

import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.instrument.InstrumentSearchQuery;
import com.trading.catalog.application.domain.model.market.Exchange;

import java.util.List;
import java.util.Optional;

public interface CatalogStorePort {

    List<Exchange> getAvailableCatalog();

    Optional<InstrumentPage> searchInstruments(InstrumentSearchQuery search);

    Optional<Instrument> findInstrumentByInstrumentId(String instrumentId);

    List<MarketChannel> getMarketChannels(String exchangeCode, String marketCode);
}
