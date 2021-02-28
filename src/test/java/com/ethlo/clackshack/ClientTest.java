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

    private static final String baseUrl = "http://clickhouse:8123";

    @Ignore
    @Test
    public void testQueryClickHouse()
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String query = "SELECT * FROM all_types " +
                    "where t_uint32 > :uint32 " +
                    "and t_string <> :string " +
                    "order by t_uint32 desc limit :max";

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
            })).thenAccept(result -> logger.info("\n{}", result)).join();
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
    public void testKillExisting() throws InterruptedException
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();
            final String query = "SELECT count() from numbers(20000000000)";
            final CompletableFuture<QueryResult> promise = client.query(query, QueryOptions.create().queryId(queryId));

            Thread.sleep(20);
            client.killQuery(queryId).whenComplete((r, e) -> logger.info("Kill command finished"));

            try
            {
                promise.whenComplete((r, e) -> logger.info("Original query died: {} - {}", r, e)).join();
            }
            catch (CompletionException exc)
            {
                assertThat(exc.getCause()).isInstanceOf(QueryAbortedException.class);
            }
        }
    }

    @Test
    public void testSelectInsertIntoProgress()
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            client.ddl("DROP TABLE IF EXISTS nums").join();

            client.ddl("CREATE TABLE nums (num UInt64) " +
                    "ENGINE = MergeTree " +
                    "ORDER BY num " +
                    "SETTINGS index_granularity = 8192;").join();

            final List<QueryProgress> progressList = new LinkedList<>();
            final CompletableFuture<QueryResult> promise = client.query("INSERT INTO nums SELECT * from numbers(30000000) as num", QueryOptions.create()
                    .progressListener(p ->
                    {
                        logger.info("Progress: {}", p);
                        progressList.add(p);
                        return true;
                    }));
            promise.join();
            assertThat(progressList).isNotEmpty();
        }
    }

    @Test
    public void testKillNonExisting()
    {
        try (final Client client = new ClientImpl(baseUrl))
        {
            final Boolean killResult = client.killQuery(UUID.randomUUID().toString()).join();
            logger.info("{}", killResult);
        }
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
                final CompletableFuture<Boolean> killResult = client.killQuery(queryId);
                killResult.thenAccept(r -> logger.info("Kill result: {}", r)).join();
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
