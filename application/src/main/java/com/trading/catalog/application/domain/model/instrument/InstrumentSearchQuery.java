package com.trading.catalog.application.domain.model.instrument;

import com.trading.catalog.application.domain.exception.ValidationException;

import java.util.Locale;

public record InstrumentSearchQuery(String exchangeCode,
                                    String marketCode,
                                    String searchText,
                                    String baseAssetCode,
                                    String quoteAssetCode,
                                    Integer limit,
                                    String cursor) {
    public InstrumentSearchQuery {
        exchangeCode = code(exchangeCode, "exchangeCode", true);
        marketCode = code(marketCode, "marketCode", true);
        baseAssetCode = code(baseAssetCode, "baseAssetCode", false);
        quoteAssetCode = code(quoteAssetCode, "quoteAssetCode", false);
        searchText = searchText == null ? "" : searchText.strip().toLowerCase(Locale.ROOT);
        if (searchText.length() > 100) {
            throw new ValidationException("searchText must be at most 100 characters");
        }
        limit = limit == null ? 50 : limit;
        if (limit < 1 || limit > 200) {
            throw new ValidationException("limit must be between 1 and 200");
        }
        if (cursor != null && (cursor.isBlank() || cursor.length() > 2048)) {
            throw new ValidationException("Invalid cursor");
        }
    }

    private static String code(String value, String name, boolean required) {
        if (value == null && !required) {
            return null;
        }
        if (value == null || value.isBlank() || value.strip().length() > 32) {
            throw new ValidationException(name + " must contain between 1 and 32 characters");
        }
        return value.strip().toUpperCase(Locale.ROOT);
    }
}
