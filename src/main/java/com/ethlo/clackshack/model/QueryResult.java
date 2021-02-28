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


import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ethlo.clackshack.ClientImpl;
import com.ethlo.clackshack.TypeConversionException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class QueryResult
{
    public static final QueryResult EMPTY = new QueryResult(Collections.emptyList(), Collections.emptyList(), new QueryStatistics(0D, 0, 0), 0, 0);

    private final List<MetaEntry> meta;
    private final List<Map<String, String>> queryData;
    private final QueryStatistics queryStatistics;
    private final int rows;
    private final long rowsBeforeLimitAtLeast;

    public QueryResult(@JsonProperty("meta") final List<MetaEntry> meta,
                       @JsonProperty("data") List<Map<String, String>> queryData,
                       @JsonProperty("statistics") QueryStatistics queryStatistics,
                       @JsonProperty("rows") final int rows,
                       @JsonProperty("rows_before_limit_at_least") final long rowsBeforeLimitAtLeast)
    {
        this.meta = meta;
        this.queryData = queryData;
        this.queryStatistics = queryStatistics;
        this.rows = rows;
        this.rowsBeforeLimitAtLeast = rowsBeforeLimitAtLeast;
    }

    public static Object convertType(final String chType, final String value)
    {
        if (value == null)
        {
            return null;
        }

        final String stripped = chType.replaceAll("Nullable\\(", "").replaceAll("\\)", "");

        switch (stripped)
        {
            case "UInt8":
            case "Int8":
                return Short.valueOf(value);
            case "UInt16":
            case "Int16":
                return Integer.valueOf(value);
            case "UInt32":
            case "Int32":
                return Long.valueOf(value);
            case "UInt64":
            case "Int64":
                return new BigInteger(value);
            case "DateTime":
                try
                {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                catch (DateTimeParseException exc)
                {
                    throw new TypeConversionException("DateTime", value, LocalDateTime.class, exc);
                }
            case "Date":
                try
                {
                    return LocalDate.parse(value);
                }
                catch (DateTimeParseException exc)
                {
                    throw new TypeConversionException("Date", value, LocalDate.class, exc);
                }
        }

        return value;
    }

    public List<Map<String, String>> getQueryData()
    {
        return queryData;
    }

    public QueryStatistics getQueryStatistics()
    {
        return queryStatistics;
    }

    public int getRows()
    {
        return rows;
    }

    public List<MetaEntry> getMeta()
    {
        return meta;
    }

    @Override
    public String toString()
    {
        try
        {
            return ClientImpl.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public long getRowsBeforeLimitAtLeast()
    {
        return rowsBeforeLimitAtLeast;
    }

    public List<Map<String, Object>> asTypedMap()
    {
        final Map<String, String> entryTypes = getEntryTypes(getMeta());

        final List<Map<String, Object>> retVal = new LinkedList<>();
        for (Map<String, String> row : getQueryData())
        {
            final Map<String, Object> typed = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : row.entrySet())
            {
                typed.put(e.getKey(), convertType(entryTypes.get(e.getKey()), e.getValue()));
            }
            retVal.add(typed);
        }

        return retVal;
    }

    private Map<String, String> getEntryTypes(final List<MetaEntry> meta)
    {
        final Map<String, String> entryTypes = new LinkedHashMap<>();
        for (final MetaEntry m : meta)
        {
            entryTypes.put(m.getName(), m.getType());
        }
        return entryTypes;
    }
}
