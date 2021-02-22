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
