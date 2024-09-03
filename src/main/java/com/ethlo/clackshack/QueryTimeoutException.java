package com.ethlo.clackshack;

/*-
 * #%L
 * ClackShack
 * %%
 * Copyright (C) 2021 - 2024 Morten Haraldsen (ethlo)
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
import java.util.Optional;

import com.ethlo.clackshack.model.QueryProgress;

public class QueryTimeoutException extends ClickHouseException
{
    private final String queryId;
    private final Duration timeout;
    private final QueryProgress queryProgress;

    public QueryTimeoutException(String queryId, Duration timeout, QueryProgress queryProgress, String message)
    {
        super(159, message);
        this.queryId = queryId;
        this.timeout = timeout;
        this.queryProgress = queryProgress;
    }

    public String getQueryId()
    {
        return queryId;
    }

    public Duration getTimeout()
    {
        return timeout;
    }

    public Optional<QueryProgress> getQueryProgress()
    {
        return Optional.ofNullable(queryProgress);
    }
}
