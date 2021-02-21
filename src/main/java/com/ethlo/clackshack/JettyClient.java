package com.ethlo.clackshack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;

import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JettyClient implements Client
{
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String CLICK_HOUSE_PROGRESS_HEADER_NAME = "X-ClickHouse-Progress";

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
    public void query(final String query, final Map<String, String> params, final QueryProgressListener queryProgressListener, final Consumer<QueryResult> callback)
    {
        final Request req = client.newRequest(baseUrl)
                .param("query", query + " format JSON")
                .param("wait_end_of_query", "1")
                .param("send_progress_in_http_headers", "1")
                .onResponseHeader((response, httpField) ->
                {
                    if (CLICK_HOUSE_PROGRESS_HEADER_NAME.equals(httpField.getName()))
                    {
                        queryProgressListener.apply(readJson(httpField.getValue(), QueryProgress.class));
                    }
                    return true;
                })
                .onComplete(result ->
                {
                    if (!result.isSucceeded())
                    {
                        throw new UncheckedIOException(new IOException(result.getFailure()));
                    }
                })
                .onResponseContent((response, content) ->
                {
                    final String strContent = StandardCharsets.UTF_8.decode(content).toString();
                    callback.accept(readJson(strContent, QueryResult.class));
                });

        params.forEach((name, value) -> req.param("param_" + name, value));

        req.send(result -> {
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
            throw new UncheckedIOException(new IOException("Error while closing http client", e));
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
