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

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.ResultSet;
import com.ethlo.clackshack.util.QueryParams;

public interface ClackShack extends AutoCloseable
{
    /**
     * Parameterized query method with progress  listener
     *
     * @param query        The SQL query
     * @param queryOptions The query options for this query
     * @return a promise that holds the result of the query
     */
    default ResultSet query(final String query, QueryOptions queryOptions)
    {
        return query(query, Collections.emptyMap(), queryOptions);
    }

    /**
     * Parameterized query method with progress  listener
     *
     * @param query  The SQL query
     * @param params The named parameters for the query
     * @return a promise that holds the result of the query
     */
    default ResultSet query(final String query, final Map<String, Object> params)
    {
        return query(query, QueryParams.asList(params), QueryOptions.DEFAULT);
    }


    /**
     * Parameterized query method with progress  listener
     *
     * @param query        The SQL query
     * @param params       The named parameters for the query
     * @param queryOptions The query options for this query
     * @return a promise that holds the result of the query
     */
    default ResultSet query(final String query, final Map<String, Object> params, final QueryOptions queryOptions)
    {
        return query(query, QueryParams.asList(params), queryOptions);
    }

    /**
     * Kill the query with the specified query ID
     *
     * @param queryId     the id of the query to kill
     * @param synchronous Whether to wait for the kill to finish
     */
    default void killQuery(String queryId, final boolean synchronous)
    {
        query("KILL QUERY WHERE query_id = :queryId " + (synchronous ? "SYNC" : ""), Collections.singletonMap("queryId", queryId), QueryOptions.DEFAULT);
    }

    /**
     * Parameterized query method with progress  listener
     *
     * @param query  The SQL query
     * @param params The named parameters for the query
     * @return a promise that holds the result of the query
     */
    ResultSet query(final String query, final List<QueryParam> params, final QueryOptions queryOptions);

    /**
     * Close any resources held by the client
     */
    void close();

    default ResultSet query(String query)
    {
        return query(query, QueryOptions.DEFAULT);
    }
}
