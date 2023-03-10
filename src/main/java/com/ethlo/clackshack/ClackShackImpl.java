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


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.QueryResult;
import com.ethlo.clackshack.model.ResultSet;
import com.ethlo.clackshack.util.QueryParams;
import com.ethlo.clackshack.util.QueryUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ClackShackImpl implements ClackShack
{
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String CLICK_HOUSE_PROGRESS_HEADER_NAME = "X-ClickHouse-Progress";
    public static final String CLICK_HOUSE_SUMMARY_HEADER_NAME = "X-ClickHouse-Summary";
    public static final String WAIT_END_OF_QUERY_PARAM = "wait_end_of_query";
    public static final String SEND_PROGRESS_IN_HTTP_HEADERS_PARAM = "send_progress_in_http_headers";
    public static final String DATABASE_PARAM = "database";
    public static final String QUERY_PARAM = "query";
    public static final String QUERY_ID_PARAM = "query_id";
    public static final String QUERY_DEFAULT_FORMAT = "default_format";
    public static final String REPLACE_RUNNING_QUERY_PARAM = "replace_running_query";
    public static final String PARAM_PREFIX = "param_";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    public static final String MAX_EXECUTION_TIME_PARAM = "max_execution_time";

    private static final Logger logger = LoggerFactory.getLogger(ClackShackImpl.class);

    static
    {
        mapper.registerModule(new JavaTimeModule());
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    private final String baseUrl;
    private final String database;
    private final HttpClient client;

    public ClackShackImpl(String baseUrl)
    {
        this(baseUrl, null);
    }

    public ClackShackImpl(String baseUrl, final String database)
    {
        this.baseUrl = baseUrl;
        this.database = database;
        this.client = new HttpClient();
        this.client.setName("clackshack");
        try
        {
            client.start();
        }
        catch (Exception e)
        {
            throw new UncheckedIOException(new IOException("Unable to start Jetty HTTP client", e));
        }
    }

    @Override
    public void ddl(String ddl)
    {
        handleDataMutating(ddl, null, QueryOptions.DEFAULT);
    }

    private void handleDataMutating(final String sql, final Map<String, Object> params, final QueryOptions queryOptions)
    {
        setDefaultDatabase(queryOptions);
        final ContentResponse response = sendRequest(sql, QueryParams.asList(params), queryOptions);
        final int status = response.getStatus();
        if (status != HttpStatus.OK_200)
        {
            final String strContent = getString(response);
            final Optional<AbstractMap.SimpleImmutableEntry<Integer, String>> error = ClickHouseErrorParser.parseError(strContent);
            if (error.isPresent())
            {
                throw ClickHouseErrorParser.handle(error.get());
            }
            throw new UncheckedIOException(new IOException("Unexpected response: " + status + " - " + strContent));
        }
    }

    @Override
    public void insert(final String sql, final Map<String, Object> params, final QueryOptions queryOptions)
    {
        setDefaultDatabase(queryOptions);
        handleDataMutating(sql, params, queryOptions);
    }

    private String getString(final ContentResponse response)
    {
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(response.getContent())).toString();
    }

    @Override
    public ResultSet query(final String query,
                           final List<QueryParam> params,
                           final QueryOptions queryOptions)
    {
        setDefaultDatabase(queryOptions);
        final QueryProgressListener queryProgressListener = queryOptions.progressListener().orElse(QueryProgressListener.NOP);
        queryProgressListener.progress(new QueryProgress(0, 0, 0));
        final ContentResponse response = sendRequest(query, params, queryOptions);

        // Process response
        final int status = response.getStatus();
        final String contentType = response.getHeaders().get(CONTENT_TYPE_HEADER_NAME);
        final String strContent = getString(response);
        if (!"".equals(strContent.trim()))
        {
            final Optional<AbstractMap.SimpleImmutableEntry<Integer, String>> error = ClickHouseErrorParser.parseError(strContent);
            if (error.isEmpty() && contentType.contains(APPLICATION_JSON_CONTENT_TYPE))
            {
                final QueryResult jsonResult = readJson(strContent, QueryResult.class);
                final long rowsRead = jsonResult.getQueryStatistics().getRowsRead();
                queryProgressListener.progress(new QueryProgress(rowsRead, jsonResult.getQueryStatistics().getBytesRead(), rowsRead));
                return new ResultSet(jsonResult);
            }
            else if (error.isPresent())
            {
                throw ClickHouseErrorParser.handle(error.get());
            }
            throw new UncheckedIOException(new IOException("Unexpected response: " + status + " - " + strContent));
        }

        if (status != HttpStatus.OK_200)
        {
            throw new UncheckedIOException(new IOException("No body content in response: " + status + " - " + strContent));
        }
        return new ResultSet(QueryResult.EMPTY);
    }

    private void setDefaultDatabase(QueryOptions queryOptions)
    {
        if (queryOptions.getDatabase().isEmpty() && database != null)
        {
            queryOptions.database(database);
        }
    }

    private ContentResponse sendRequest(final String query, final List<QueryParam> params, final QueryOptions queryOptions)
    {
        final String queryId = queryOptions.queryId().orElse(UUID.randomUUID().toString());
        final String q = params != null ? QueryUtil.format(query, params) : query;
        logger.debug("Running query with id {}: {}", queryId, q);
        final AtomicBoolean killedMarker = new AtomicBoolean(false);

        final Request req = client.newRequest(baseUrl)
                .method(HttpMethod.POST)
                .param(QUERY_DEFAULT_FORMAT, "JSON")
                .param(QUERY_PARAM, q)
                .param(QUERY_ID_PARAM, Objects.requireNonNull(queryId, "queryId must not be null"))
                .param(REPLACE_RUNNING_QUERY_PARAM, queryOptions.replaceQuery() ? "1" : "0");

        queryOptions.getDatabase().ifPresent(db ->
        {
            logger.debug("Default DB set to {} for query", db);
            req.param(DATABASE_PARAM, db);
        });

        // Enable progress headers
        final AtomicReference<QueryProgress> lastSentProgress = new AtomicReference<>();
        final QueryProgressListener queryProgressListener = queryOptions.progressListener().orElse(QueryProgressListener.NOP);
        if (queryOptions.progressListener().isPresent())
        {
            req.onResponseHeader((response, httpField) ->
            {
                if (CLICK_HOUSE_PROGRESS_HEADER_NAME.equals(httpField.getName()))
                {
                    final QueryProgress progress = readJson(httpField.getValue(), QueryProgress.class);

                    if (!progress.equals(lastSentProgress.get()))
                    {
                        lastSentProgress.set(progress);
                        final boolean continueProcessing = queryProgressListener.progress(progress);
                        if (!continueProcessing && !killedMarker.get())
                        {
                            logger.info("Progress listener returned false for query {}, attempting to kill query", queryId);
                            final boolean result = killQuery(queryId);
                            logger.info("Killed {}", result);
                            killedMarker.set(true);
                            throw new QueryAbortedException();
                        }
                    }
                }
                return true;
            });
            req.param(WAIT_END_OF_QUERY_PARAM, "1");
            req.param(SEND_PROGRESS_IN_HTTP_HEADERS_PARAM, "1");
        }

        // Enable max query time
        queryOptions.maxExecutionTime().ifPresent(duration -> req.param(MAX_EXECUTION_TIME_PARAM, Long.toString(duration.toSeconds())));

        Optional.ofNullable(params).ifPresent(p -> p.forEach(param -> req.param(PARAM_PREFIX + param.getName(), Optional.ofNullable(param.getValue()).map(Object::toString).orElse(null))));

        // Perform request
        try
        {
            final ContentResponse response = req.send();

            // Send final notification when done
            if (!killedMarker.get())
            {

                Optional.ofNullable(response).flatMap(contentResponse -> Optional.ofNullable(contentResponse.getHeaders().get(CLICK_HOUSE_SUMMARY_HEADER_NAME))
                        .map(summary -> readJson(summary, QueryProgress.class))).ifPresent(summary ->
                {
                    final QueryProgress finalProgress = new QueryProgress(summary.getReadRows(), summary.getReadBytes(), summary.getTotalRowsToRead());
                    if (!finalProgress.equals(lastSentProgress.get()))
                    {
                        queryProgressListener.progress(finalProgress);
                    }
                });
            }

            return response;
        }
        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException(exc));
        }
        catch (TimeoutException exc)
        {
            throw new UncheckedIOException(new IOException(exc));
        }
        catch (ExecutionException exc)
        {
            throw new UncheckedIOException(new IOException(exc.getCause()));
        }
    }

    @Override
    public void close()
    {
        try
        {
            client.stop();
        }
        catch (Exception e)
        {
            throw new UncheckedIOException(new IOException("Error while closing HTTP client", e));
        }
    }

    private <T> T readJson(final String data, Class<T> type)
    {
        try
        {
            return mapper.readValue(data, type);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Unable to parse JSON", e);
        }
    }
}
