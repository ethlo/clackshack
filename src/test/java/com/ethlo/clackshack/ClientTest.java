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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.clickhouse.jdbc.ClickHouseDriver;
import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.model.ResultSet;
import com.ethlo.clackshack.model.Row;
import com.ethlo.clackshack.util.IOUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);
    private static final String baseUrl = "http://localhost:8123";

    @Test
    public void testQueryAllDataTypes()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final DataSource ds = new SingleConnectionDataSource(new ClickHouseDriver().connect("jdbc:clickhouse://localhost:8123?compress=false", new Properties()), true);
            final NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(ds);
            tpl.update("drop table if exists data_types", Map.of());
            tpl.update(IOUtil.readClasspath("datatypes_table.ddl"), Map.of());

            final Map<String, Object> params = Map.of(
                    "t_uint8", 122,
                    "t_uint16", 5553,
                    "t_string_map", Map.of("hello", "world"),
                    "t_string_array", new String[]{"hello", "world"}
            );
            tpl.update("""
                    INSERT INTO data_types
                    (t_uint8, t_uint16, t_string_map, t_string_array)
                    VALUES
                    (:t_uint8, :t_uint16, :t_string_map, :t_string_array)""", params);

            final String query = "SELECT * FROM data_types limit :limit";

            final int limit = 2_000;

            final Map<String, Object> queryParams = Map.of(
                    "limit", limit,
                    "string", "abcdefasdadasd"
            );

            final ResultSet result = clackShack.query(query, queryParams);
            logger.info("\n{}", result);
            assertThat(result).hasSize(1);

            final Row firstRow = result.getRow(0);
            assertThat(firstRow.get("t_ipv4")).isInstanceOf(Inet4Address.class);
            assertThat(firstRow.get("t_ipv6")).isInstanceOf(Inet6Address.class);
            assertThat(firstRow.get("t_string_map")).isInstanceOf(ObjectNode.class);

            assertThat(result.asMap()).hasSize(1);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
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
            final String query = "SELECT count() from numbers(2000000000000)";
            final AtomicReference<Exception> exceptionRef = runInSeparateThreadAndFetchException(clackShack, query, QueryOptions.create().queryId(queryId));

            Thread.sleep(100);
            clackShack.killQuery(queryId, true);
            logger.info("Kill command finished");

            assertThat(exceptionRef.get()).isInstanceOf(QueryAbortedException.class);
        }
    }


    @Test
    public void testKillNonExisting()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            clackShack.killQuery(UUID.randomUUID().toString(), true);
            logger.info("Killed non-existing");
        }
    }

    @Test
    public void testQueryExecutionTimeExceeded()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String query = "SELECT count() from numbers(20000000000)";
            final QueryTimeoutException exc = assertThrows(QueryTimeoutException.class, () -> clackShack.query(query, QueryOptions.create()
                    .maxExecutionTime(Duration.ofSeconds(1))
                    .progressListener(p ->
                    {
                        logger.info("{}", p);
                        progressList.add(p);
                        return true;
                    })));
            assertThat(exc.getQueryId()).isNotNull();
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
                clackShack.killQuery(queryId, true);
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

    @Test
    public void testLargerResultSet()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();

            final String query = "SELECT * from numbers(100000)";
            final ResultSet resultSet = clackShack.query(query, QueryOptions.create()
                    .queryId(queryId)
                    .progressListener(QueryProgressListener.LOGGER));
            resultSet.getRow(99_999);
        }
    }

    @Test
    public void testLargerRequestSet()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String queryId = UUID.randomUUID().toString();

            final String query = "SELECT rand()" + ",rand()".repeat(1000);
            final ResultSet resultSet = clackShack.query(query, QueryOptions.create()
                    .queryId(queryId)
                    .progressListener(QueryProgressListener.LOGGER));
            resultSet.getRow(0);
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
                    logger.debug("Background query {} finished: {}", queryId, query);
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
