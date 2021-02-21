package com.ethlo.clackshack;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.ProgressListener;
import com.ethlo.clackshack.model.QueryProgressData;
import com.ethlo.clackshack.model.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mikael.urlbuilder.UrlBuilder;

public class Java11Client implements Client
{
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String CLICK_HOUSE_PROGRESS_HEADER_NAME = "X-ClickHouse-Progress";
    private final Logger logger = LoggerFactory.getLogger(Java11Client.class);
    private final HttpClient httpClient;
    private final String baseUrl;

    public Java11Client(String baseUrl)
    {
        this(baseUrl, HttpClient.newHttpClient());
    }

    public Java11Client(final String baseUrl, final HttpClient httpClient)
    {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public void query(final String query, final Map<String, String> params, final ProgressListener progressListener, final Consumer<QueryResult> callback)
    {
        UrlBuilder urlBuilder = UrlBuilder.fromString(baseUrl)
                .addParameter("query", query + " format JSON")
                .addParameter("wait_end_of_query", "1")
                .addParameter("send_progress_in_http_headers", "1");

        // Add query parameters
        for (Map.Entry<String, String> e : params.entrySet())
        {
            urlBuilder = urlBuilder.addParameter("param_" + e.getKey(), e.getValue());
        }

        final HttpRequest request = HttpRequest.newBuilder(urlBuilder.toUri()).build();

        logger.info("Requesting {}", request.uri());
        final CompletableFuture<HttpResponse<InputStream>> future = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
        logger.info("Request sent");

        while (!future.isDone())
        {
            // TODO: How can we access the headers before the content is starting to be sent

            //logger.info("Checking");
            Optional.ofNullable(future.getNow(null))
                    .ifPresent(resp -> resp.headers()
                            .allValues(CLICK_HOUSE_PROGRESS_HEADER_NAME)
                            .forEach(h -> progressListener.apply(readJson(h, QueryProgressData.class))));
            /*
            try
            {
                logger.info("Waiting");
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            */
        }

        logger.info("Reading body");
        final QueryResult result = future.thenApply(HttpResponse::body)
                .thenApply(inputStream -> {
                    try
                    {
                        return mapper.readValue(inputStream, QueryResult.class);
                    }
                    catch (IOException e)
                    {
                        throw new UncheckedIOException("Unable to parse JSON", e);
                    }
                }).join();
        callback.accept(result);
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
