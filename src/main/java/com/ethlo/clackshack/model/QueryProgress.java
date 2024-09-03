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


import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryProgress
{
    public static final QueryProgress ZERO = new QueryProgress(0, 0, 0);
    final private long readRows;
    final private long readBytes;
    final private long totalRowsToRead;

    public QueryProgress(@JsonProperty("read_rows") long readRows,
                         @JsonProperty("read_bytes") long readBytes,
                         @JsonProperty("total_rows_to_read") long totalRowsToRead)
    {
        this.readRows = readRows;
        this.readBytes = readBytes;
        this.totalRowsToRead = Math.max(totalRowsToRead, readRows);
    }

    public double getPercentDone()
    {
        if (getReadRows() != 0 && getTotalRowsToRead() != 0)
        {
            return Math.min(getReadRows() / (double) getTotalRowsToRead() * 100, 100D);
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
        return "QueryProgress{progress=" + getPercentDone() + "%, " +
                "readRows=" + readRows +
                ", readBytes=" + readBytes +
                ", totalRowsToRead=" + totalRowsToRead +
                '}';
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryProgress that = (QueryProgress) o;
        return readRows == that.readRows && readBytes == that.readBytes && totalRowsToRead == that.totalRowsToRead;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(readRows, readBytes, totalRowsToRead);
    }
}
