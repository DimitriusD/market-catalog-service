package com.trading.catalog.storage.catalog;

import com.trading.catalog.application.domain.exception.ValidationException;
import com.trading.catalog.application.domain.model.Asset;
import com.trading.catalog.application.domain.model.channel.MarketChannel;
import com.trading.catalog.application.domain.model.channel.ChannelParameter;
import com.trading.catalog.application.domain.model.channel.ChannelParameterOption;
import com.trading.catalog.application.domain.model.instrument.Instrument;
import com.trading.catalog.application.domain.model.instrument.InstrumentPage;
import com.trading.catalog.application.domain.model.instrument.InstrumentSearch;
import com.trading.catalog.application.domain.model.market.Exchange;
import com.trading.catalog.application.domain.model.market.Market;
import com.trading.catalog.application.port.output.CatalogStorePort;
import com.trading.catalog.storage.entity.AssetEntity;
import com.trading.catalog.storage.entity.ChannelEntity;
import com.trading.catalog.storage.entity.ExchangeEntity;
import com.trading.catalog.storage.entity.ExchangeMarketChannelEntity;
import com.trading.catalog.storage.entity.ExchangeMarketChannelParamAllowedValueEntity;
import com.trading.catalog.storage.entity.ExchangeMarketChannelParamEntity;
import com.trading.catalog.storage.entity.ExchangeMarketEntity;
import com.trading.catalog.storage.entity.InstrumentEntity;
import com.trading.catalog.storage.entity.MarketTypeEntity;
import com.trading.catalog.storage.repository.AssetRepository;
import com.trading.catalog.storage.repository.ChannelRepository;
import com.trading.catalog.storage.repository.ExchangeMarketChannelParamAllowedValueRepository;
import com.trading.catalog.storage.repository.ExchangeMarketChannelParamRepository;
import com.trading.catalog.storage.repository.ExchangeMarketChannelRepository;
import com.trading.catalog.storage.repository.ExchangeMarketRepository;
import com.trading.catalog.storage.repository.ExchangeRepository;
import com.trading.catalog.storage.repository.InstrumentRepository;
import com.trading.catalog.storage.repository.MarketTypeRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CatalogStore implements CatalogStorePort {

    private static final String SEARCH_INSTRUMENTS = """
            SELECT i.id, i.instrument_id, i.exchange_symbol, i.display_symbol,
                   b.code AS base_code, b.name AS base_name,
                   q.code AS quote_code, q.name AS quote_name
            FROM instruments i
                     JOIN assets b ON b.id = i.base_asset_id
                     JOIN assets q ON q.id = i.quote_asset_id
            WHERE i.exchange_market_id = :marketId
              AND i.enabled
              AND (CAST(:baseAssetCode AS VARCHAR) IS NULL OR b.code = :baseAssetCode)
              AND (CAST(:quoteAssetCode AS VARCHAR) IS NULL OR q.code = :quoteAssetCode)
              AND (CAST(:pattern AS VARCHAR) IS NULL
                   OR lower(i.exchange_symbol) LIKE :pattern ESCAPE '\\'
                   OR lower(i.display_symbol) LIKE :pattern ESCAPE '\\'
                   OR lower(b.name) LIKE :pattern ESCAPE '\\')
              AND (CAST(:afterSymbol AS VARCHAR) IS NULL
                   OR i.exchange_symbol COLLATE "C" > :afterSymbol
                   OR (i.exchange_symbol = :afterSymbol AND i.id > :afterId))
            ORDER BY i.exchange_symbol COLLATE "C", i.id
            LIMIT :limit
            """;

    private final ExchangeRepository exchanges;
    private final MarketTypeRepository marketTypes;
    private final ExchangeMarketRepository exchangeMarkets;
    private final ChannelRepository channels;
    private final ExchangeMarketChannelRepository exchangeMarketChannels;
    private final ExchangeMarketChannelParamRepository channelParams;
    private final ExchangeMarketChannelParamAllowedValueRepository channelParamValues;
    private final InstrumentRepository instruments;
    private final AssetRepository assets;
    private final NamedParameterJdbcTemplate jdbc;

    public CatalogStore(ExchangeRepository exchanges,
                        MarketTypeRepository marketTypes,
                        ExchangeMarketRepository exchangeMarkets,
                        ChannelRepository channels,
                        ExchangeMarketChannelRepository exchangeMarketChannels,
                        ExchangeMarketChannelParamRepository channelParams,
                        ExchangeMarketChannelParamAllowedValueRepository channelParamValues,
                        InstrumentRepository instruments,
                        AssetRepository assets,
                        NamedParameterJdbcTemplate jdbc) {
        this.exchanges = exchanges;
        this.marketTypes = marketTypes;
        this.exchangeMarkets = exchangeMarkets;
        this.channels = channels;
        this.exchangeMarketChannels = exchangeMarketChannels;
        this.channelParams = channelParams;
        this.channelParamValues = channelParamValues;
        this.instruments = instruments;
        this.assets = assets;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exchange> getAvailableCatalog() {
        Map<Long, MarketTypeEntity> marketTypeById = byId(marketTypes.findAll(), MarketTypeEntity::getId);
        Map<Long, ChannelEntity> channelById = byId(channels.findAll(), ChannelEntity::getId);

        Map<Long, List<ExchangeMarketEntity>> marketsByExchange = exchangeMarkets.findAll().stream()
                .filter(ExchangeMarketEntity::isEnabled)
                .collect(Collectors.groupingBy(ExchangeMarketEntity::getExchangeId));
        Map<Long, List<ExchangeMarketChannelEntity>> channelsByMarket = exchangeMarketChannels.findAll().stream()
                .filter(ExchangeMarketChannelEntity::isEnabled)
                .collect(Collectors.groupingBy(ExchangeMarketChannelEntity::getExchangeMarketId));
        Map<Long, List<ExchangeMarketChannelParamEntity>> paramsByChannel = channelParams.findAll().stream()
                .collect(Collectors.groupingBy(ExchangeMarketChannelParamEntity::getExchangeMarketChannelId));
        Map<Long, List<ExchangeMarketChannelParamAllowedValueEntity>> valuesByParam = channelParamValues.findAll().stream()
                .collect(Collectors.groupingBy(ExchangeMarketChannelParamAllowedValueEntity::getChannelParamId));

        return exchanges.findAll().stream()
                .filter(ExchangeEntity::isEnabled)
                .sorted(Comparator.comparing(ExchangeEntity::getCode))
                .map(exchange -> {
                    Exchange.ExchangeBuilder builder = Exchange.builder()
                            .code(exchange.getCode())
                            .displayName(exchange.getName())
                            .enabled(true);

                    marketsByExchange.getOrDefault(exchange.getId(), List.of()).stream()
                            .sorted(Comparator.comparing(ExchangeMarketEntity::getCode))
                            .forEach(em -> {
                                Market.MarketBuilder market = Market.builder()
                                        .code(em.getCode())
                                        .marketType(marketTypeById.get(em.getMarketTypeId()).getCode())
                                        .displayName(em.getName())
                                        .enabled(true);

                                channelsByMarket.getOrDefault(em.getId(), List.of()).stream()
                                        .sorted(Comparator.comparing(emc -> channelById.get(emc.getChannelId()).getCode()))
                                        .forEach(emc -> market.channel(
                                                toMarketChannel(emc, true, channelById,
                                                        paramsByChannel.getOrDefault(emc.getId(), List.of()),
                                                        paramId -> valuesByParam.getOrDefault(paramId, List.of()))));

                                builder.market(market.build());
                            });

                    return builder.build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InstrumentPage> searchInstruments(InstrumentSearch search) {
        Optional<MarketRef> market = findMarket(search.exchangeCode(), search.marketCode())
                .filter(MarketRef::available);
        if (market.isEmpty()) {
            return Optional.empty();
        }

        Cursor after = search.cursor() == null ? null : Cursor.decode(search.cursor());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("marketId", market.get().market().getId())
                .addValue("baseAssetCode", search.baseAssetCode())
                .addValue("quoteAssetCode", search.quoteAssetCode())
                .addValue("pattern", search.q().isEmpty() ? null : "%" + escapeLike(search.q()) + "%")
                .addValue("afterSymbol", after == null ? null : after.exchangeSymbol())
                .addValue("afterId", after == null ? null : after.id())
                .addValue("limit", search.limit() + 1);

        String exchangeCode = market.get().exchange().getCode();
        String marketCode = market.get().market().getCode();
        List<InstrumentRow> rows = jdbc.query(SEARCH_INSTRUMENTS, params, (rs, i) -> new InstrumentRow(
                rs.getLong("id"),
                Instrument.builder()
                        .instrumentId(rs.getString("instrument_id"))
                        .exchangeCode(exchangeCode)
                        .marketCode(marketCode)
                        .baseAsset(Asset.builder().code(rs.getString("base_code")).name(rs.getString("base_name")).build())
                        .quoteAsset(Asset.builder().code(rs.getString("quote_code")).name(rs.getString("quote_name")).build())
                        .exchangeSymbol(rs.getString("exchange_symbol"))
                        .displaySymbol(rs.getString("display_symbol"))
                        .enabled(true)
                        .build()));

        boolean hasMore = rows.size() > search.limit();
        List<InstrumentRow> page = hasMore ? rows.subList(0, search.limit()) : rows;
        String nextCursor = null;
        if (hasMore) {
            InstrumentRow last = page.getLast();
            nextCursor = new Cursor(last.instrument().getExchangeSymbol(), last.id()).encode();
        }
        return Optional.of(new InstrumentPage(page.stream().map(InstrumentRow::instrument).toList(), nextCursor));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instrument> findInstrumentByInstrumentId(String instrumentId) {
        return instruments.findByInstrumentId(instrumentId).map(instrument -> {
            ExchangeMarketEntity em = exchangeMarkets.findById(instrument.getExchangeMarketId()).orElseThrow();
            ExchangeEntity exchange = exchanges.findById(em.getExchangeId()).orElseThrow();
            Map<Long, AssetEntity> assetById = byId(
                    assets.findAllById(List.of(instrument.getBaseAssetId(), instrument.getQuoteAssetId())),
                    AssetEntity::getId);

            return Instrument.builder()
                    .instrumentId(instrument.getInstrumentId())
                    .exchangeCode(exchange.getCode())
                    .marketCode(em.getCode())
                    .baseAsset(toAsset(assetById.get(instrument.getBaseAssetId())))
                    .quoteAsset(toAsset(assetById.get(instrument.getQuoteAssetId())))
                    .exchangeSymbol(instrument.getExchangeSymbol())
                    .displaySymbol(instrument.getDisplaySymbol())
                    .enabled(instrument.isEnabled() && em.isEnabled() && exchange.isEnabled())
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketChannel> getMarketChannels(String exchangeCode, String marketCode) {
        Optional<MarketRef> market = findMarket(exchangeCode, marketCode);
        if (market.isEmpty()) {
            return List.of();
        }
        boolean marketAvailable = market.get().available();
        Map<Long, ChannelEntity> channelById = byId(channels.findAll(), ChannelEntity::getId);

        return exchangeMarketChannels.findByExchangeMarketId(market.get().market().getId()).stream()
                .map(emc -> toMarketChannel(emc, marketAvailable, channelById,
                        channelParams.findByExchangeMarketChannelId(emc.getId()),
                        channelParamValues::findByChannelParamId))
                .sorted(Comparator.comparing(MarketChannel::getCode))
                .toList();
    }

    private Optional<MarketRef> findMarket(String exchangeCode, String marketCode) {
        return exchanges.findByCode(exchangeCode)
                .flatMap(exchange -> exchangeMarkets.findByExchangeIdAndCode(exchange.getId(), marketCode)
                        .map(market -> new MarketRef(exchange, market)));
    }

    private static MarketChannel toMarketChannel(ExchangeMarketChannelEntity emc,
                                                 boolean marketAvailable,
                                                 Map<Long, ChannelEntity> channelById,
                                                 List<ExchangeMarketChannelParamEntity> parameters,
                                                 Function<Long, List<ExchangeMarketChannelParamAllowedValueEntity>> valuesForParameter) {
        ChannelEntity channel = channelById.get(emc.getChannelId());
        MarketChannel.MarketChannelBuilder builder = MarketChannel.builder()
                .code(channel.getCode())
                .name(channel.getName())
                .enabled(marketAvailable && emc.isEnabled());

        parameters.stream()
                .sorted(Comparator.comparing(ExchangeMarketChannelParamEntity::getParamKey))
                .map(parameter -> toChannelParameter(parameter, valuesForParameter.apply(parameter.getId())))
                .forEach(builder::parameter);

        return builder.build();
    }

    private static ChannelParameter toChannelParameter(ExchangeMarketChannelParamEntity parameter,
                                                       List<ExchangeMarketChannelParamAllowedValueEntity> values) {
        List<ExchangeMarketChannelParamAllowedValueEntity> availableValues = values.stream()
                .filter(ExchangeMarketChannelParamAllowedValueEntity::isEnabled)
                .sorted(Comparator.comparing(ExchangeMarketChannelParamAllowedValueEntity::getSortOrder))
                .toList();

        return ChannelParameter.builder()
                .key(parameter.getParamKey())
                .required(parameter.isRequired())
                .defaultValue(availableValues.stream()
                        .filter(ExchangeMarketChannelParamAllowedValueEntity::isDefault)
                        .map(ExchangeMarketChannelParamAllowedValueEntity::getValue)
                        .findFirst()
                        .orElse(null))
                .allowedValues(availableValues.stream()
                        .map(value -> ChannelParameterOption.builder()
                                .value(value.getValue())
                                .displayName(value.getDisplayName() == null ? value.getValue() : value.getDisplayName())
                                .build())
                        .toList())
                .build();
    }

    private static Asset toAsset(AssetEntity asset) {
        return asset == null ? null : Asset.builder()
                .code(asset.getCode())
                .name(asset.getName())
                .build();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static <T> Map<Long, T> byId(List<T> entities, Function<T, Long> idGetter) {
        return entities.stream().collect(Collectors.toMap(idGetter, Function.identity()));
    }

    private record MarketRef(ExchangeEntity exchange, ExchangeMarketEntity market) {
        boolean available() {
            return exchange.isEnabled() && market.isEnabled();
        }
    }

    private record InstrumentRow(long id, Instrument instrument) {
    }

    /** Keyset position {@code (exchange_symbol, id)} of the last returned row, base64url-encoded. */
    private record Cursor(String exchangeSymbol, long id) {

        String encode() {
            String raw = id + ":" + exchangeSymbol;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        static Cursor decode(String cursor) {
            try {
                String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                int separator = raw.indexOf(':');
                return new Cursor(raw.substring(separator + 1), Long.parseLong(raw.substring(0, separator)));
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                throw new ValidationException("Invalid cursor");
            }
        }
    }
}
