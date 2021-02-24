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


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.QueryResult;

public class ClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    private static final String baseUrl = "http://localhost:8123";

    @Test
    public void testQueryClickHouse()
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String query = "SELECT id, created, code, result FROM validations " +
                    "where id > :id " +
                    "and code <> :code " +
                    "and created < :max_created " +
                    "and result = :result " +
                    "order by created desc limit :max";

            final Map<String, Object> params = Map.of(
                    "id", 0L,
                    "max", 10,
                    "code", "abcdefasdadasd",
                    "max_created", LocalDateTime.now(),
                    "result", "y"
            );
            client.query(query, params).thenAccept(result ->
            {
                logger.info("\n{}", result);
                final Object created = result.asTypedMap().get(0).get("created");
                assertThat(created).isNotNull();
                assertThat(created).isInstanceOf(LocalDateTime.class);
            }).join();
        }
    }

    @Test
    public void testQueryProgress()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String query = "SELECT count() from numbers(2000000000)";
            client.query(query, QueryOptions.create().progressListener(p ->
            {
                logger.info("{}", p);
                progressList.add(p);
                return true;
            })).thenAcceptAsync(result -> logger.info("\n{}", result)).join();
        }

        assertThat(progressList).isNotEmpty();
    }

    @Test
    public void testQueryProgressAbort()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final Client client = new ClientImpl(baseUrl))
        {
            final AtomicInteger counter = new AtomicInteger();
            final String query = "SELECT count() from numbers(2000000000)";
            final CompletableFuture<QueryResult> promise = client.query(query, QueryOptions.create()
                    .queryId("some-progress-query")
                    .progressListener(p ->
                    {
                        logger.info("{}", p);
                        progressList.add(p);
                        return counter.incrementAndGet() < 3;
                    }));

            try
            {
                promise.join();
                fail("Should have thrown exception");
            }
            catch (CompletionException exc)
            {
                assertThat(exc.getCause()).isInstanceOf(QueryAbortedException.class);
            }
        }

        assertThat(progressList).isNotEmpty();
    }

    @Test
    @Ignore
    public void testQueryExecutionTimeExceeded()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String query = "SELECT count() from numbers(2000000000)";
            final CompletableFuture<QueryResult> promise = client.query(query, QueryOptions.create()
                    .maxExecutionTime(Duration.ofSeconds(1))
                    .progressListener(p ->
                    {
                        logger.info("{}", p);
                        progressList.add(p);
                        return true;
                    }));

            try
            {
                promise.join();
                fail("Should have thrown exception");
            }
            catch (CompletionException exc)
            {
                assertThat(exc.getCause()).isInstanceOf(QueryAbortedException.class);
            }
        }

        assertThat(progressList).isNotEmpty();
    }

    @Test
    public void testSameQueryIdShouldThrow() throws InterruptedException
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();
            final QueryOptions options = QueryOptions.create().queryId(queryId);

            final String query = "SELECT count() from numbers(10000000000)";
            client.query(query, options).thenAccept(result -> logger.info("\n{}", result));
            Thread.sleep(10);

            // Same query again, with same id
            try
            {
                client.query(query, options).thenAccept(result -> logger.info("\n{}", result)).get();
                fail("Should fail as the same query id already is in progress");
            }
            catch (ExecutionException expected)
            {
                assertThat(expected.getCause()).isInstanceOf(DuplicateQueryIdException.class);

                // Kill the initial query to avoid having to wait for it
                final CompletableFuture<QueryResult> killResult = client.killQuery(queryId);
                killResult.thenAccept(r -> logger.info("Kill result: {}", r.asTypedMap())).join();
            }
        }
    }

    @Test
    public void testSameQueryIdShouldReplace() throws InterruptedException
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();

            final String query = "SELECT count() from numbers(1000000000)";
            final CompletableFuture<QueryResult> original = client.query(query, QueryOptions.create()
                    .queryId(queryId)
                    .progressListener(QueryProgressListener.LOGGER));
            Thread.sleep(50);

            // Same query again, with same id
            final QueryResult replacement = client.query(query, QueryOptions.create()
                    .queryId(queryId)
                    .replaceQuery(true)
            ).join();
            assertThat(replacement).isNotNull();

            try
            {
                original.get();
                fail("Should throw");
            }
            catch (ExecutionException exc)
            {
                assertThat(exc.getCause()).isInstanceOf(QueryAbortedException.class);
            }
        }
    }
}
