package com.ethlo.clackshack.model;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2021 Morten Haraldsen (ethlo)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ethlo.clackshack.util.JsonUtil;

public class QueryParam
{
    private final String name;
    private final String type;
    private final Object value;

    public QueryParam(final String name, final String type, final Object value)
    {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
    }

    public QueryParam(final String name, final String type)
    {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.value = null;
    }

    public static QueryParam of(final String name, final byte value)
    {
        return new QueryParam(name, "Int8", value);
    }

    public static QueryParam of(final String name, final short value)
    {
        return new QueryParam(name, "Int16", value);
    }

    public static QueryParam of(final String name, final int value)
    {
        return new QueryParam(name, "Int32", value);
    }

    public static QueryParam of(final String name, final long value)
    {
        return new QueryParam(name, "Int64", value);
    }

    public static QueryParam of(final String name, final String value)
    {
        return new QueryParam(name, "String", value);
    }

    public static QueryParam of(final String name, final boolean value)
    {
        return new QueryParam(name, "UInt8", value ? 1 : 0);
    }

    public static QueryParam of(final String name, final float value)
    {
        return new QueryParam(name, "Float32", value);
    }

    public static QueryParam of(final String name, final double value)
    {
        return new QueryParam(name, "Float64", value);
    }

    public static QueryParam of(final String name, final LocalDateTime dateTime)
    {
        return new QueryParam(name, "String", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(dateTime));
    }

    public static QueryParam ofNull(String name, String type)
    {
        return new QueryParam(name, type);
    }

    public static <K, V> QueryParam of(String name, Map<K, V> map, String keyType, String valueType)
    {
        return new QueryParam(name, "Map(%s, %s)".formatted(keyType, valueType), JsonUtil.string(map));
    }

    public static <V> QueryParam of(String name, List<V> array, String valueType)
    {
        return new QueryParam(name, "Array(%s)".formatted(valueType), JsonUtil.string(array));
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }
}
