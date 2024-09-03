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


import static com.ethlo.clackshack.util.JsonUtil.readJson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.InputStreamResponseListener;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.QueryResult;
import com.ethlo.clackshack.model.ResultSet;
import com.ethlo.clackshack.util.QueryUtil;

public class ClackShackImpl implements ClackShack
{
    public static final String CLICKHOUSE_PROGRESS_HEADER_NAME = "X-ClickHouse-Progress";
    public static final String CLICKHOUSE_QUERY_ID_HEADER_NAME = "X-ClickHouse-Query-Id";
    public static final String CLICKHOUSE_SUMMARY_HEADER_NAME = "X-ClickHouse-Summary";
    public static final String CLICKHOUSE_EXCEPTION_CODE_HEADER_NAME = "X-ClickHouse-Exception-Code";

    public static final String WAIT_END_OF_QUERY_PARAM = "wait_end_of_query";
    public static final String SEND_PROGRESS_IN_HTTP_HEADERS_PARAM = "send_progress_in_http_headers";
    public static final String DATABASE_PARAM = "database";
    public static final String QUERY_ID_PARAM = "query_id";
    public static final String QUERY_DEFAULT_FORMAT = "default_format";
    public static final String REPLACE_RUNNING_QUERY_PARAM = "replace_running_query";
    public static final String PARAM_PREFIX = "param_";
    public static final String MAX_EXECUTION_TIME_PARAM = "max_execution_time";


    private static final Logger logger = LoggerFactory.getLogger(ClackShackImpl.class);

    private final String baseUrl;
    private final String database;
    private final HttpClient client;
    private final Duration timeout;

    public ClackShackImpl(String baseUrl)
    {
        this(baseUrl, null, Duration.ofSeconds(30));
    }

    public ClackShackImpl(String baseUrl, final String database, final Duration timeout)
    {
        this.baseUrl = baseUrl;
        this.database = database;
        this.timeout = timeout;
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

    private String getString(final InputStreamResponseListener response)
    {
        try
        {
            return new String(response.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ResultSet query(final String query,
                           final List<QueryParam> params,
                           final QueryOptions queryOptions)
    {
        final QueryProgressListener queryProgressListener = queryOptions.progressListener().orElse(QueryProgressListener.NOP);
        queryProgressListener.progress(new QueryProgress(0, 0, 0));
        final ResponseData responseData = sendRequest(query, params, queryOptions);

        // Process response
        final int status = responseData.response().getStatus();
        final String body = getString(responseData.contentListener());
        logger.trace("Response body data: {}", body);

        final Optional<Integer> errorCode = Optional.ofNullable(responseData.response().getHeaders().get(CLICKHOUSE_EXCEPTION_CODE_HEADER_NAME)).map(Integer::parseInt);
        if (errorCode.isPresent())
        {
            final Optional<AbstractMap.SimpleImmutableEntry<Integer, String>> error = ClickHouseErrorParser.parseError(body);
            throw ClickHouseErrorParser.handle(errorCode.get(), error.map(Map.Entry::getValue).orElse("Unknown error"), queryOptions, responseData.response());
        }

        if (status != HttpStatus.OK_200)
        {
            throw new UncheckedIOException(new IOException("No " + CLICKHOUSE_EXCEPTION_CODE_HEADER_NAME + " present and HTTP status was " + status + ": " + body));
        }

        if (body.trim().isEmpty())
        {
            logger.debug("No response body data");
            return new ResultSet(QueryResult.EMPTY);
        }

        final QueryResult jsonResult = readJson(body, QueryResult.class);
        queryProgressListener.progress(new QueryProgress(jsonResult.getQueryStatistics().getRowsRead(), jsonResult.getQueryStatistics().getBytesRead(), jsonResult.getQueryStatistics().getTotalRowsToRead()));
        return new ResultSet(jsonResult);
    }

    private ResponseData sendRequest(final String query, final List<QueryParam> params, QueryOptions queryOptions)
    {
        final String queryId = queryOptions.queryId().orElse(UUID.randomUUID().toString());
        final String q = params != null ? QueryUtil.format(query, params) : query;
        logger.debug("Running query with id {}: {}", queryId, q);
        final AtomicBoolean killedMarker = new AtomicBoolean(false);

        if (queryOptions.getDatabase().isEmpty() && database != null)
        {
            queryOptions = queryOptions.database(database);
        }

        final Request req = client.newRequest(baseUrl)
                .method(HttpMethod.POST)
                .param(QUERY_DEFAULT_FORMAT, "JSON")
                .param(QUERY_ID_PARAM, Objects.requireNonNull(queryId, "queryId must not be null"))
                .param(REPLACE_RUNNING_QUERY_PARAM, queryOptions.replaceQuery() ? "1" : "0")
                .body(new StringRequestContent(q));

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
                if (CLICKHOUSE_PROGRESS_HEADER_NAME.equals(httpField.getName()))
                {
                    final String strContent = httpField.getValue();
                    logger.debug("JSON progress from header: {}", strContent);
                    final QueryProgress progress = readJson(strContent, QueryProgress.class);

                    if (!progress.equals(lastSentProgress.get()))
                    {
                        lastSentProgress.set(progress);
                        final boolean continueProcessing = queryProgressListener.progress(progress);
                        if (!continueProcessing && !killedMarker.get())
                        {
                            logger.info("Progress listener returned false for query {}, attempting to kill query", queryId);
                            killQuery(queryId, true);
                            killedMarker.set(true);
                            throw new QueryAbortedException(queryId);
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

        Optional.ofNullable(params)
                .ifPresent(p -> p.forEach(param -> req.param(PARAM_PREFIX + param.getName(),
                        Optional.ofNullable(param.getValue())
                                .map(Object::toString)
                                .orElse(null)
                )));

        // Perform request
        try
        {
            final InputStreamResponseListener listener = new InputStreamResponseListener();
            req.send(listener);
            final Response response = listener.get(timeout.getSeconds(), TimeUnit.SECONDS);

            // Send final notification when done
            if (!killedMarker.get())
            {
                Optional.ofNullable(response).flatMap(contentResponse -> Optional.ofNullable(contentResponse.getHeaders().get(CLICKHOUSE_SUMMARY_HEADER_NAME))
                        .map(summary -> readJson(summary, QueryProgress.class))).ifPresent(summary ->
                {
                    final QueryProgress finalProgress = new QueryProgress(summary.getReadRows(), summary.getReadBytes(), summary.getTotalRowsToRead());
                    if (!finalProgress.equals(lastSentProgress.get()))
                    {
                        queryProgressListener.progress(finalProgress);
                    }
                });
            }

            return new ResponseData(response, listener);
        }
        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException(exc));
        }
        catch (TimeoutException exc)
        {
            throw new QueryTimeoutException(queryId, timeout, lastSentProgress.get(), "Query " + queryId + " timed out after " + timeout.getSeconds() + " seconds with a query progress of " + lastSentProgress.get().getPercentDone() + "%");
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
}
