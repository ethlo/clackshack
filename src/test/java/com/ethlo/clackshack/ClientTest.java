package com.ethlo.clackshack;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryProgress;

public class ClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    private static final String baseUrl = "http://localhost:8123";

    @Test
    public void testQueryClickHouse() throws ExecutionException, InterruptedException
    {
        try (final Client client = new JettyClient(baseUrl))
        {
            final String query = "SELECT id, created, code, result FROM validations " +
                    "where id > :foo " +
                    "and code <> :code " +
                    "and created < :max_created " +
                    "and result = :result " +
                    "order by created desc limit :max";
            final List<QueryParam> params = Arrays.asList(
                    QueryParam.of("foo", 0L),
                    QueryParam.of("max", 10),
                    QueryParam.of("code", "abcdefasdadasd"),
                    QueryParam.of("max_created", LocalDateTime.now()),
                    QueryParam.of("result", "y")
            );
            client.query(query, params, progress -> true).thenAccept(result -> logger.info("\n{}", result)).get();
        }
    }

    @Test
    public void testQueryProgressClickHouse() throws ExecutionException, InterruptedException
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final Client client = new JettyClient(baseUrl))
        {
            final String query = "SELECT count() from numbers(10000000000)";
            client.query(query, progressList::add).thenAccept(result -> logger.info("\n{}", result)).get();
        }

        assertThat(progressList).isNotEmpty();
    }
}