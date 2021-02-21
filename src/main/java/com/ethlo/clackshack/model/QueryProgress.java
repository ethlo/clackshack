package com.ethlo.clackshack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryProgress
{
    final private long readRows;
    final private long readBytes;
    final private long totalRowsToRead;

    public QueryProgress(@JsonProperty("read_rows") long readRows,
                         @JsonProperty("read_bytes") long readBytes,
                         @JsonProperty("total_rows_to_read") long totalRowsToRead)
    {
        this.readRows = readRows;
        this.readBytes = readBytes;
        this.totalRowsToRead = totalRowsToRead;
    }

    public double getPercentDone()
    {
        if (getReadBytes() != 0 && getTotalRowsToRead() != 0)
        {
            return getReadRows() / (double) getTotalRowsToRead() * 100;
        }
        return 0;
    }

    public long getReadRows()
    {
        return readRows;
    }

    public long getReadBytes()
    {
        return readBytes;
    }

    public long getTotalRowsToRead()
    {
        return totalRowsToRead;
    }

    @Override
    public String toString()
    {
        return "QueryProgressData{progress=" + getPercentDone() + "%, " +
                "readRows=" + readRows +
                ", readBytes=" + readBytes +
                ", totalRowsToRead=" + totalRowsToRead +
                '}';
    }
}