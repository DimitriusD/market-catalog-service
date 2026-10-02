package com.trading.catalog.application.domain.model.instrument;

import java.util.List;

public record InstrumentPage(List<Instrument> items, String nextCursor) {
    public InstrumentPage {
        items = List.copyOf(items);
    }
}
