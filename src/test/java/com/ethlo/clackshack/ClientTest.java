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
import static org.junit.Assert.assertThrows;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.ResultSet;
import com.ethlo.clackshack.model.Row;
import com.ethlo.clackshack.util.IOUtil;

public class ClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);
    private static final String baseUrl = "http://localhost:8123";
    @Rule
    public LogTestName logTestName = new LogTestName();

    @Test
    public void testQueryAllDataTypes()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            clackShack.ddl("drop table if exists data_types");
            clackShack.ddl(IOUtil.readClasspath("datatypes_table.ddl"));

            final String query = "SELECT * FROM data_types limit :limit";

            final int limit = 2_000;

            final Map<String, Object> params = Map.of(
                    "limit", limit,
                    "string", "abcdefasdadasd"
            );

            final ResultSet result = clackShack.query(query, params);
            logger.info("\n{}", result);
            assertThat(result).isNotNull();
            assertThat(result).hasSize(limit);

            final Row firstRow = result.getRow(0);
            assertThat(firstRow.get("t_ipv4")).isInstanceOf(Inet4Address.class);
            assertThat(firstRow.get("t_ipv6")).isInstanceOf(Inet6Address.class);

            assertThat(result.asMap()).hasSize(limit);
        }
    }

    @Test
    public void testQueryProgress()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String query = "SELECT count() from numbers(2000000000)";
            final ResultSet result = clackShack.query(query, QueryOptions.create().progressListener(p ->
            {
                logger.info("{}", p);
                progressList.add(p);
                return true;
            }));
            logger.info("\n{}", result.getRow(0));
        }

        assertThat(progressList).isNotEmpty();
    }

    @Test
    public void testQueryProgressAbort()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final AtomicInteger counter = new AtomicInteger();
            final String query = "SELECT count() from numbers(2000000000)";

            assertThrows(QueryAbortedException.class, () ->
            {
                clackShack.query(query, QueryOptions.create()
                        .queryId("some-progress-query")
                        .progressListener(p ->
                        {
                            logger.info("{}", p);
                            progressList.add(p);
                            return counter.incrementAndGet() < 3;
                        }));

                fail("Should have thrown exception");
            });
        }

        assertThat(progressList).isNotEmpty();
    }

    @Test
    public void testKillExisting() throws InterruptedException
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();
            final String query = "SELECT count() from numbers(200000000000)";
            final AtomicReference<Exception> exceptionRef = runInSeparateThreadAndFetchException(clackShack, query, QueryOptions.create().queryId(queryId));

            Thread.sleep(100);
            clackShack.killQuery(queryId);
            logger.info("Kill command finished");

            assertThat(exceptionRef.get()).isInstanceOf(QueryAbortedException.class);
        }
    }

    @Test
    public void testInsert()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            clackShack.ddl("CREATE TABLE IF NOT EXISTS insert_me (id UInt32, message String) ENGINE = MergeTree ORDER BY id");
            final Map<String, Object> params = new LinkedHashMap<>();
            params.put("id", 123);
            params.put("message", "Hello world");
            clackShack.insert("INSERT INTO insert_me VALUES (:id, :message)", params);
        }
    }

    @Test
    public void testSelectInsertIntoProgress()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            clackShack.ddl("DROP TABLE IF EXISTS nums");

            clackShack.ddl("CREATE TABLE nums (num UInt64) " +
                    "ENGINE = MergeTree " +
                    "ORDER BY num " +
                    "SETTINGS index_granularity = 8192");

            final List<QueryProgress> progressList = new LinkedList<>();
            clackShack.insert("INSERT INTO nums SELECT * from numbers(30000000) as num", QueryOptions.create()
                    .progressListener(p ->
                    {
                        logger.info("Progress: {}", p);
                        progressList.add(p);
                        return true;
                    }));
            assertThat(progressList).isNotEmpty();
        }
    }

    @Test
    public void testKillNonExisting()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final Boolean killResult = clackShack.killQuery(UUID.randomUUID().toString());
            logger.info("Killed non-existing: {}", killResult);
        }
    }

    @Ignore
    @Test
    public void testQueryExecutionTimeExceeded()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String query = "SELECT count() from numbers(2000000000)";
            try
            {
                clackShack.query(query, QueryOptions.create()
                        .maxExecutionTime(Duration.ofSeconds(1))
                        .progressListener(p ->
                        {
                            logger.info("{}", p);
                            progressList.add(p);
                            return true;
                        }));

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
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();
            final String query = "SELECT count() from numbers(100000000000)";
            final QueryOptions options = QueryOptions.create().queryId(queryId);
            runInSeparateThreadAndFetchException(clackShack, query, options);
            Thread.sleep(100);

            // Same query again, with same id
            try
            {
                final ResultSet result2 = clackShack.query(query, options);
                logger.info("\n{}", result2);
                fail("Should fail as the same query id already is in progress");
            }
            catch (Exception expected)
            {
                assertThat(expected).isInstanceOf(DuplicateQueryIdException.class);

                // Kill the initial query to avoid having to wait for it
                clackShack.killQuery(queryId);
            }
        }
    }

    @Test
    public void testSameQueryIdShouldReplace() throws InterruptedException
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();

            final String query = "SELECT count() from numbers(3000000000)";
            final AtomicReference<Exception> exceptionRef = runInSeparateThreadAndFetchException(clackShack, query, QueryOptions.create()
                    .queryId(queryId)
                    .progressListener(QueryProgressListener.LOGGER));

            Thread.sleep(100);

            // Same query again, with same id
            final ResultSet replacement = clackShack.query(query, QueryOptions.create()
                    .queryId(queryId)
                    .replaceQuery(true));

            assertThat(replacement).isNotNull();
            assertThat(exceptionRef.get()).isInstanceOf(QueryAbortedException.class);
        }
    }

    private AtomicReference<Exception> runInSeparateThreadAndFetchException(ClackShack clackShack, String query, QueryOptions queryId)
    {
        final AtomicReference<Exception> exceptionRef = new AtomicReference<>();
        new Thread()
        {
            public void run()
            {
                try
                {
                    setName("old-query-thread");
                    clackShack.query(query, queryId);
                }
                catch (Exception exc)
                {
                    exceptionRef.set(exc);
                }
            }
        }.start();
        return exceptionRef;
    }
}
