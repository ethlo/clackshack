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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryResult;
import com.ethlo.clackshack.util.QueryParams;

public interface Client extends AutoCloseable
{
    /**
     * Simple, non-parameterized query method
     *
     * @param queryId The ID to assign this query
     * @param query   The SQL query
     * @return a promise that holds the result of the query
     */
    default CompletableFuture<QueryResult> query(final String queryId, final String query)
    {
        return query(queryId, false, query, Collections.emptyList(), QueryProgressListener.NOP);
    }

    /**
     * Simple, non-parameterized query method with progress listener
     *
     * @param queryId               The ID to assign this query
     * @param query                 The SQL query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    default CompletableFuture<QueryResult> query(final String queryId, final String query, final QueryProgressListener queryProgressListener)
    {
        return query(queryId, false, query, Collections.emptyList(), queryProgressListener);
    }

    /**
     * Parameterized query method with progress  listener
     *
     * @param queryId               The ID to assign this query
     * @param query                 The SQL query
     * @param params                The named parameters for the query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    default CompletableFuture<QueryResult> query(final String queryId, final String query, final Map<String, Object> params, final QueryProgressListener queryProgressListener)
    {
        return query(queryId, false, query, QueryParams.asList(params), queryProgressListener);
    }

    /**
     * Parameterized query method with progress  listener
     *
     * @param queryId               The ID to assign this query
     * @param replaceExistingQuery  Whether to replace the existing query or throw an exception if it is already in progress
     * @param query                 The SQL query
     * @param params                The named parameters for the query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    default CompletableFuture<QueryResult> query(final String queryId, final boolean replaceExistingQuery, final String query, final Map<String, Object> params, final QueryProgressListener queryProgressListener)
    {
        return query(queryId, replaceExistingQuery, query, QueryParams.asList(params), queryProgressListener);
    }

    /**
     * Kill the query with the specified query ID
     *
     * @param queryId the id of the query to kill
     * @return True if the query was found, false if it did not exist
     */
    default CompletableFuture<Boolean> killQuery(String queryId)
    {
        return query(queryId, true, "SELECT 1", Collections.emptyList(), p -> true).thenApply(Objects::nonNull);
    }

    /**
     * Parameterized query method with progress  listener
     *
     * @param queryId               The ID to assign this query
     * @param replaceExistingQuery  Whether to replace the existing query or throw an exception if it is already in progress
     * @param query                 The SQL query
     * @param params                The named parameters for the query
     * @param queryProgressListener A progress listener that will be called every time ClickHouse provides an updated progress header
     * @return a promise that holds the result of the query
     */
    CompletableFuture<QueryResult> query(final String queryId, final boolean replaceExistingQuery, final String query, final List<QueryParam> params, final QueryProgressListener queryProgressListener);

    /**
     * Close any resources held by the client
     */
    void close();
}
