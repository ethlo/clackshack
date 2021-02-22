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
            client.query(query, p ->
            {
                logger.info("{}", p);
                progressList.add(p);
                return true;
            }).thenAccept(result -> logger.info("\n{}", result)).get();
        }

        assertThat(progressList).isNotEmpty();
    }
}
