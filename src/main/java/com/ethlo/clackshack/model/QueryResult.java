package com.ethlo.clackshack.model;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2017 - 2021 Morten Haraldsen (ethlo)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import com.ethlo.clackshack.JettyClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class QueryResult
{
    private final List<MetaEntry> meta;
    private final List<Map<String, Object>> queryData;
    private final QueryStatistics queryStatistics;
    private final int rows;
    private final long rowsBeforeLimitAtLeast;

    public QueryResult(@JsonProperty("meta") final List<MetaEntry> meta,
                       @JsonProperty("data") List<Map<String, Object>> queryData,
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

    public List<Map<String, Object>> getQueryData()
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
            return JettyClient.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
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
}
