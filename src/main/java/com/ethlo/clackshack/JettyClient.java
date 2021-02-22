package com.ethlo.clackshack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public static String format(String format, List<QueryParam> values)
    {
        final StringBuilder formatter = new StringBuilder(format);
        final List<Object> valueList = new ArrayList<>();
        final Matcher matcher = Pattern.compile(":(\\w+)").matcher(format);

        while (matcher.find())
        {
            String key = matcher.group(1);
            String formatKey = String.format(":%s", key);
            final int index = formatter.indexOf(formatKey);

            if (index != -1)
            {
                formatter.replace(index, index + formatKey.length(), "%s");
                final QueryParam param = getValue(key, values);
                valueList.add(String.format("{%s:%s}", param.getName(), param.getType()));
            }
        }

        return String.format(formatter.toString(), valueList.toArray());
    }

    private static QueryParam getValue(final String key, final List<QueryParam> values)
    {
        return values.stream().filter(v -> v.getName().equals(key)).findFirst().orElseThrow(() -> new IllegalArgumentException("No such param name: " + key));
    }

    @Override
    public CompletableFuture<QueryResult> query(final String query, final List<QueryParam> params, final QueryProgressListener queryProgressListener)
    {
        final String rewritten = format(query, params);

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
