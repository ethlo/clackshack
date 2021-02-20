package com.ethlo.clackshack;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryStatistics
{
    private final long elapsed;

    public QueryStatistics(@JsonProperty("elapsed") final long elapsed,
                           @JsonProperty("rows_read") final long rows_read,
                            @JsonProperty("bytes_read") final long bytes_read)
    {
        this.elapsed = elapsed;
    }

    public long getElapsed()
    {
        return elapsed;
    }
}
