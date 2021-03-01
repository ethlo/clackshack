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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Row
{
    private final Map<String, Object> delegate;

    public Row(final Map<String, Object> delegate)
    {
        this.delegate = delegate;
    }

    public int size()
    {
        return delegate.size();
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public Object get(final String column)
    {
        return delegate.get(column);
    }

    public <T> T get(final int columnIndex, final Class<T> type)
    {
        final List<Object> columns = new LinkedList<>(delegate.values());
        final Object val = columns.get(columnIndex);
        return type.cast(val);
    }

    public <T> T get(final String columnName, final Class<T> type)
    {
        final Object val = Optional.ofNullable(delegate.get(columnName)).orElseThrow(() -> new IllegalArgumentException("No column with name " + columnName + " found"));
        return type.cast(val);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Row row = (Row) o;
        return Objects.equals(delegate, row.delegate);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(delegate);
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

    public Map<String, Object> asMap()
    {
        return Collections.unmodifiableMap(delegate);
    }
}
