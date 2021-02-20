package com.ethlo.clackshack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mikael.urlbuilder.UrlBuilder;

public class Client
{
    public static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final String baseUrl;

    public Client(String baseUrl)
    {
        this(baseUrl, HttpClient.newHttpClient());
    }

    public Client(final String baseUrl, final HttpClient httpClient)
    {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public void getColumnTypes(String tableName, Consumer<QueryResult> resultListener)
    {
        final String query = "select * from " + tableName + " format JSON";
        query(query, resultListener);
    }

    public void query(final String query, final Consumer<QueryResult> resultListener)
    {
        query(query, Collections.emptyMap(), ProgressListener.NOP, resultListener);
    }

    public void query(final String query, final Map<String, String> params, final ProgressListener progressListener, final Consumer<QueryResult> callback)
    {
        UrlBuilder urlBuilder = UrlBuilder.fromString(baseUrl)
                .addParameter("query", query + " format JSON")
                .addParameter("wait_end_of_query", "1")
                .addParameter("send_progress_in_http_headers", "1");
        for (Map.Entry<String, String> e : params.entrySet())
        {
            urlBuilder = urlBuilder.addParameter("param_" + e.getKey(), e.getValue());
        }

        final HttpRequest request = HttpRequest.newBuilder(urlBuilder.toUri()).build();
        final QueryResult data = httpClient.sendAsync(request, (HttpResponse.BodyHandler<String>) responseInfo ->
        {
            final List<String> progressHeaders = responseInfo.headers().allValues("X-ClickHouse-Progress");
            final Optional<Boolean> continueQuery = Optional.ofNullable(progressHeaders.isEmpty() ? null : progressHeaders.get(progressHeaders.size() - 1))
                    .map(p -> readJson(p, QueryProgressData.class))
                    .map(progressListener);

            // TODO: Attempt to cancel query if applicable

            return null;
        })
                .thenApply(HttpResponse::body)
                .thenApply(d -> readJson(d, QueryResult.class))
                .join();
        callback.accept(data);
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
