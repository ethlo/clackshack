package com.ethlo.clackshack;

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
import java.util.Optional;

public class QueryOptions
{
    public static final QueryOptions DEFAULT = new QueryOptions(null, false, null, null);

    private final String queryId;
    private final boolean replaceQuery;
    private final Duration maxExecutionTime;
    private final QueryProgressListener progressListener;

    public QueryOptions(final String queryId, final boolean replaceQuery, final Duration maxExecutionTime, final QueryProgressListener progressListener)
    {
        this.queryId = queryId;
        this.replaceQuery = replaceQuery;
        this.maxExecutionTime = maxExecutionTime;
        this.progressListener = progressListener;
    }

    public static QueryOptions create()
    {
        return DEFAULT;
    }

    /**
     * Whether running a query with the same ID should replace the existing query, or throw an {@link DuplicateQueryIdException}
     *
     * @return True if replace, false throws exception
     */
    public boolean replaceQuery()
    {
        return replaceQuery;
    }

    /**
     * Returns the maximum time allowed for this execution to finish
     *
     * @return Returns the maximum time allowed for this execution to finish
     */
    public Optional<Duration> maxExecutionTime()
    {
        return Optional.ofNullable(maxExecutionTime);
    }

    /**
     * The id of this query if any was assigned
     *
     * @return The id of this query if any was assigned
     */
    public Optional<String> queryId()
    {
        return Optional.ofNullable(queryId);
    }

    /**
     * A progress listener that will be called every time ClickHouse provides an updated progress header
     *
     * @return A progress listener that will be called every time ClickHouse provides an updated progress header
     */
    public Optional<QueryProgressListener> progressListener()
    {
        return Optional.ofNullable(progressListener);
    }

    public QueryOptions progressListener(QueryProgressListener progressListener)
    {
        return new QueryOptions(this.queryId, this.replaceQuery, this.maxExecutionTime, progressListener);
    }

    public QueryOptions queryId(final String queryId)
    {
        return new QueryOptions(queryId, this.replaceQuery, this.maxExecutionTime, progressListener);
    }

    public QueryOptions maxExecutionTime(final Duration maxExecutionTime)
    {
        return new QueryOptions(queryId, this.replaceQuery, maxExecutionTime, progressListener);
    }

    public QueryOptions replaceQuery(final boolean replaceQuery)
    {
        if (queryId == null && replaceQuery)
        {
            throw new IllegalStateException("queryId is required when using replaceQuery");
        }
        return new QueryOptions(this.queryId, replaceQuery, maxExecutionTime, progressListener);
    }
}
