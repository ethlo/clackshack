# ClackShack

[![Maven Central](https://img.shields.io/maven-central/v/com.ethlo.clackshack/clackshack.svg)](https://search.maven.org/artifact/com.ethlo.clackshack/clackshack)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/2291d61125f44037b92f56893d13fe49)](https://www.codacy.com/gh/ethlo/clackshack/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ethlo/clackshack&amp;utm_campaign=Badge_Grade)
[![GitHub issues open](https://img.shields.io/github/issues/ethlo/clackshack.svg)](https://github.com/ethlo/jsons2xsd/issues)

Unofficial ClickHouse HTTP query interface client.

## Main features

* Easy to use
* Support query progress updates via listener interface.

## Query parameters

Use `:name` placeholders and pass the values as a `Map<String, Object>` or a list of `QueryParam`.
A `Collection` or array value becomes an `Array(T)` parameter, with `T` inferred from the elements, so it can be used with `IN` and `NOT IN`:

```java
clackShack.query("SELECT * FROM events WHERE product_id NOT IN (:ids)", Map.of("ids", List.of("milk", "flour")));
```

* Mixed number elements are widened (e.g. `Int32` and `Int64` give `Int64`, `Int32` and `Float64` give `Float64`); `Int64` with a float, other mixes and `null` elements are rejected.
* An empty collection becomes `Array(Nothing)`: `IN` matches nothing and `NOT IN` matches everything.
* For element types that cannot be inferred (e.g. `BigDecimal`, `UInt64`, maps), use `QueryParam.of(name, list, type)`.
* Parameters are sent in the URL, so very large lists can exceed ClickHouse's `http_max_uri_size` (1 MiB by default).
