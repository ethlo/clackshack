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


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryResult;

public interface Client extends AutoCloseable
{
    /**
     * Simple, non-parameterized query method
     *
     * @param query The SQL query
     * @return a promise that holds the result of the query
     */
    default CompletableFuture<QueryResult> query(final String query)
    {
        return query(query, Collections.emptyList(), QueryProgressListener.NOP);
    }

    /**
     * Simple, non-parameterized query method with progress listener
     *
     * @param query                 The SQL query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    default CompletableFuture<QueryResult> query(final String query, final QueryProgressListener queryProgressListener)
    {
        return query(query, Collections.emptyList(), queryProgressListener);
    }

    /**
     * Parameterized query method with progress  listener
     *
     * @param query                 The SQL query
     * @param params                The named parameters for the query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    CompletableFuture<QueryResult> query(final String query, final Map<String, Object> params, final QueryProgressListener queryProgressListener);

    /**
     * Parameterized query method with progress  listener
     *
     * @param query                 The SQL query
     * @param params                The named parameters for the query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    CompletableFuture<QueryResult> query(final String query, final List<QueryParam> params, final QueryProgressListener queryProgressListener);

    /**
     * Close any resources held by the client
     */
    void close();
}
