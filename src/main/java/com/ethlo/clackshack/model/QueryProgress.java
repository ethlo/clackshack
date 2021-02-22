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
