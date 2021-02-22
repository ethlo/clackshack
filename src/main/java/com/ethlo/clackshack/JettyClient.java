package com.ethlo.clackshack;

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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.QueryResult;
import com.ethlo.clackshack.util.QueryParams;
import com.ethlo.clackshack.util.QueryUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JettyClient implements Client
{
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String CLICK_HOUSE_PROGRESS_HEADER_NAME = "X-ClickHouse-Progress";
    public static final String WAIT_END_OF_QUERY = "wait_end_of_query";
    public static final String SEND_PROGRESS_IN_HTTP_HEADERS = "send_progress_in_http_headers";
    public static final String QUERY = "query";
    public static final String PARAM_PREFIX = "param_";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    static
    {
        mapper.registerModule(new JavaTimeModule());
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    private final String baseUrl;
    private final HttpClient client;

    public JettyClient(String baseUrl)
    {
        this.baseUrl = baseUrl;
        this.client = new HttpClient();
        this.client.setName("ch-client");
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
    public CompletableFuture<QueryResult> query(final String query, final Map<String, Object> params, final QueryProgressListener queryProgressListener)
    {
        return query(query, QueryParams.asList(params), queryProgressListener);
    }

    @Override
    public CompletableFuture<QueryResult> query(final String query, final List<QueryParam> params, final QueryProgressListener queryProgressListener)
    {
        final String rewritten = QueryUtil.format(query, params);

        final Request req = client.newRequest(baseUrl)
                .param(QUERY, rewritten + " format JSON")
                .param(WAIT_END_OF_QUERY, "1")
                .param(SEND_PROGRESS_IN_HTTP_HEADERS, "1")
                .onResponseHeader((response, httpField) ->
                {
                    if (CLICK_HOUSE_PROGRESS_HEADER_NAME.equals(httpField.getName()))
                    {
                        queryProgressListener.apply(readJson(httpField.getValue(), QueryProgress.class));
                    }
                    return true;
                });

        params.forEach((param) -> req.param(PARAM_PREFIX + param.getName(), param.getValue().toString()));

        final CompletableFuture<ContentResponse> completable = new CompletableFuture<>();
        req.send(new CompletableFutureResponseListener(completable));
        return completable.thenApply(response ->
        {
            final String contentType = response.getHeaders().get(CONTENT_TYPE_HEADER_NAME);
            final String strContent = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(response.getContent())).toString();
            if (contentType != null && contentType.contains(APPLICATION_JSON_CONTENT_TYPE))
            {
                return readJson(strContent, QueryResult.class);
            }
            else
            {
                throw new UncheckedIOException(new IOException(strContent));
            }
        });
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
