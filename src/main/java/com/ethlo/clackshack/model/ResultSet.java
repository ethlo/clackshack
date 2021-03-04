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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.TypeConversionException;

public class ResultSet implements Iterable<Row>
{
    private static final Logger logger = LoggerFactory.getLogger(ResultSet.class);

    private final List<Row> data;

    public ResultSet(QueryResult result)
    {
        final Map<String, String> entryTypes = getEntryTypes(result.getMeta());

        this.data = new ArrayList<>(result.getRows());
        for (Map<String, String> rowData : result.getQueryData())
        {
            final Map<String, Object> typed = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : rowData.entrySet())
            {
                typed.put(e.getKey(), convertType(entryTypes.get(e.getKey()), e.getValue()));
            }
            data.add(new Row(typed));
        }
    }

    public static Object convertType(final String chType, final String value)
    {
        if (value == null)
        {
            return null;
        }

        final String nullableStripped = stripNullable(chType);
        final String baseType = stripParameters(nullableStripped);

        logger.trace("Type: {}", baseType);
        final DataTypes.DataType<?> type = DataTypes.match(baseType).orElseThrow(() -> new IllegalArgumentException("Unknown type: " + baseType));
        try
        {
            return type.getParser().apply(value);
        }
        catch (Exception exc)
        {
            throw new TypeConversionException(type.getName(), value, type.getType(), exc);
        }
    }

    private static String stripParameters(final String nullableStripped)
    {
        final Pattern nullablePattern = Pattern.compile("^([a-zA-Z0-9]+).*\\)");
        final Matcher nullableMatcher = nullablePattern.matcher(nullableStripped);
        if (nullableMatcher.find())
        {
            return nullableMatcher.group(1);
        }
        else
        {
            return nullableStripped;
        }
    }

    private static String stripNullable(final String chType)
    {
        final Pattern nullablePattern = Pattern.compile("^Nullable\\((.*)\\)$");
        final Matcher nullableMatcher = nullablePattern.matcher(chType);
        if (nullableMatcher.find())
        {
            return nullableMatcher.group(1);
        }
        else
        {
            return chType;
        }
    }

    public <T> T get(int rowIndex, int columnIndex, Class<T> type)
    {
        return getRow(rowIndex).get(columnIndex, type);
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

    @Override
    public Iterator<Row> iterator()
    {
        return data.iterator();
    }

    @Override
    public void forEach(final Consumer<? super Row> action)
    {
        data.forEach(action);
    }

    @Override
    public Spliterator<Row> spliterator()
    {
        return data.spliterator();
    }

    @Override
    public String toString()
    {
        return "ResultSet{" +
                "size=" + size() +
                '}';
    }

    private int size()
    {
        return data.size();
    }

    public Row getRow(final int rowIndex)
    {
        if (rowIndex < 0 || rowIndex > data.size() - 1)
        {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }
        return data.get(rowIndex);
    }

    public List<Map<String, Object>> asMap()
    {
        final List<Map<String, Object>> result = new ArrayList<>(size());
        for (Row row : data)
        {
            result.add(row.asMap());
        }
        return result;
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }
}
