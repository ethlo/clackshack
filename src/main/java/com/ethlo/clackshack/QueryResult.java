package com.ethlo.clackshack;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class QueryResult
{
    private final List<MetaEntry> meta;
    private final List<Map<String, String>> queryData;
    private final QueryStatistics queryStatistics;
    private final int rows;

    public QueryResult(@JsonProperty("meta") final List<MetaEntry> meta,
                       @JsonProperty("data") List<Map<String, String>> queryData,
                       @JsonProperty("statistics") QueryStatistics queryStatistics,
                       @JsonProperty("rows") final int rows)
    {
        this.meta = meta;
        this.queryData = queryData;
        this.queryStatistics = queryStatistics;
        this.rows = rows;
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
            return Client.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
