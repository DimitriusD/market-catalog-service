# Market Catalog Service

Owns the market catalog: exchanges, market types, stream channels with their parameters and allowed
values, assets and instruments. Serves it over REST to the UI and to `trading-control-service`, which
resolves instruments and validates stream channels against it.

Extracted from `trading-control-service` as-is (same API shape, same Flyway migrations).

Hexagonal layout (ports & adapters), Gradle multi-module.

## Modules

| Module | Purpose |
|--------|---------|
| `application` | Domain models, `MarketCatalogService` / `CatalogStorePort`, `MarketCatalogServiceImpl`. No framework dependencies. |
| `infrastructure/app` | Spring Boot entrypoint and wiring (`InfrastructureConfig`). |
| `infrastructure/rest-api` | Controllers and MapStruct mappers over interfaces generated from the contract. |
| `infrastructure/rest-api/market-catalog-service-open-api` | The OpenAPI contract, published as `com.trading.contracts:market-catalog-service-openapi`. |
| `infrastructure/jdbc-storage-adapter` | PostgreSQL storage via Spring Data JDBC (`CatalogStore`). Flyway migrations. |

## Channel domain model

`domain/model/channel` contains the shared model for both catalog and channel-capability responses:

- `MarketChannel`: a channel supported by an exchange market, with its effective availability and parameters.
- `ChannelParameter`: a parameter rule with `required`, `defaultValue` and `allowedValues`.
- `ChannelParameterOption`: an allowed value and its display name.

The storage adapter assembles this model using the same mapping for both reads. The REST adapter projects it
into the existing UI and control-service DTOs. `ChannelCapability` is an API representation, not a separate
domain model. A disabled default is omitted from the available options and produces `defaultValue: null`.

## API

| Method | Path | Consumer |
|--------|------|----------|
| `GET` | `/api/v1/catalog` | UI: exchanges → markets (`code` + `marketType`) → channels → param rules (`required`, `defaultValue`, `values`) |
| `GET` | `/api/v1/instruments?exchangeCode&marketCode&q&baseAssetCode&quoteAssetCode&limit&cursor` | UI: instrument picker; `limit` 1–200 (default 50), pass `nextCursor` back as `cursor` |
| `GET` | `/api/v1/instruments/{instrumentId}` | control-service: resolve the instrument of a new stream |
| `GET` | `/api/v1/markets/{exchangeCode}/{marketCode}/channel-capabilities` | control-service: validate stream channels/params |

`instrumentId` contains `|` (`BINANCE|SPOT|BTC|USDT`) and must be URL-encoded in the path (`%7C`).

**Availability.** An exchange, market, channel, param value or instrument is available only when it and everything
above it are enabled. `/catalog` and instrument search return only available items (search answers 404 for an
unavailable market); `/instruments/{id}` and `channel-capabilities` return the item with the effective `enabled` flag.

**Markets.** A market has its own `code` (unique per exchange, used in requests) and a `marketType`; several markets
of one exchange may share a type. Several instruments (contracts) may share the same base/quote pair.

## Database

Migrations live in `infrastructure/jdbc-storage-adapter/src/main/resources/db/migration/<table>/`
(schema) and `<table>/data/` (seed data), timestamp-versioned (`V<yyyyMMddHHmmss>__*.sql`). Numeric surrogate
keys stay; instrument search uses keyset pagination over `(exchange_symbol COLLATE "C", id)`.

## Tests

`./gradlew build` runs unit tests plus Testcontainers integration tests (storage against Flyway-seeded Postgres,
and the REST API end to end), so Docker must be running.

## Run locally

```bash
docker compose up -d
./gradlew :infrastructure:app:bootRun
```

## Publishing the contract

```bash
./gradlew :infrastructure:rest-api:market-catalog-service-open-api:publishToMavenLocal   # local consumers
./gradlew :infrastructure:rest-api:market-catalog-service-open-api:publish               # GitHub Packages (gpr.user / gpr.key)
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APP_PORT` | `8097` | HTTP port |
| `POSTGRES_PORT` | `5433` | Postgres port (`catalog` DB, user/password `trading`) |
