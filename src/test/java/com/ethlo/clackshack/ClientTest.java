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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            client.query(query, params, progress -> true).thenAccept(result ->
            {
                logger.info("\n{}", result);

                final Object created = result.asTypedMap().get(0).get("created");
                assertThat(created).isNotNull();
            }).get();
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
