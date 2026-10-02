package com.trading.catalog.storage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("instruments")
public class InstrumentEntity {

    @Id
    private Long id;

    @Column("instrument_id")
    private String instrumentId;

    @Column("exchange_market_id")
    private Long exchangeMarketId;

    @Column("base_asset_id")
    private Long baseAssetId;

    @Column("quote_asset_id")
    private Long quoteAssetId;

    @Column("exchange_symbol")
    private String exchangeSymbol;

    @Column("display_symbol")
    private String displaySymbol;

    private String name;
    private String description;

    private boolean enabled;

    @Column("price_precision")
    private Integer pricePrecision;

    @Column("quantity_precision")
    private Integer quantityPrecision;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
