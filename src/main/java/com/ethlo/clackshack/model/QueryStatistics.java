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

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryStatistics
{
    private final double elapsed;
    private final long rows_read;
    private final long bytesRead;

    public QueryStatistics(@JsonProperty("elapsed") final double elapsed,
                           @JsonProperty("rows_read") final long rowsRead,
                           @JsonProperty("bytes_read") final long bytesRead)
    {
        this.elapsed = elapsed;
        this.rows_read = rowsRead;
        this.bytesRead = bytesRead;
    }

    public Duration getElapsed()
    {
        return Duration.ofNanos((long) (elapsed * 1_000_000_000));
    }

    public long getRowsRead()
    {
        return rows_read;
    }

    public long getBytesRead()
    {
        return bytesRead;
    }


}
