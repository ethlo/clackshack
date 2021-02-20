package com.ethlo.clackshack;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryProgressData
{
    final private long readRows;
    final private long writtenRows;
    final private long readBytes;
    final private long writtenBytes;
    final private long totalRowsToRead;

    public QueryProgressData(@JsonProperty("read_rows") long readRows,
                             @JsonProperty("written_rows") long writtenRows,
                             @JsonProperty("read_bytes") long readBytes,
                             @JsonProperty("written_bytes") long writtenBytes,
                             @JsonProperty("total_rows_to_read") long totalRowsToRead)
    {
        this.readRows = readRows;
        this.writtenRows = writtenRows;
        this.readBytes = readBytes;
        this.writtenBytes = writtenBytes;
        this.totalRowsToRead = totalRowsToRead;
    }

    public long getReadRows()
    {
        return readRows;
    }

    public long getWrittenRows()
    {
        return writtenRows;
    }

    public long getReadBytes()
    {
        return readBytes;
    }

    public long getWrittenBytes()
    {
        return writtenBytes;
    }

    public long getTotalRowsToRead()
    {
        return totalRowsToRead;
    }
}