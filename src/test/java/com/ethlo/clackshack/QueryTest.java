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

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ethlo.clackshack.model.DataTypes;
import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.ResultSet;

public class QueryTest
{
    private static final String baseUrl = "http://localhost:8123";

    @Test
    public void testInQuery()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String query = "SELECT number from numbers(2000) where number in (:ids)";
            final List<QueryParam> params = List.of(QueryParam.of("ids", List.of(4, 12, 989), DataTypes.UINT_64));
            final ResultSet result = clackShack.query(query, params);
            assertThat(result.size()).isEqualTo(3);
            final List<Long> found = result.stream().mapToLong(r -> r.get("number", BigInteger.class).longValue()).boxed().toList();
            assertThat(found).containsExactly(4L, 12L, 989L);
        }
    }

    @Test
    public void testInAndNotInWithInferredArrayParams()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final String query = "SELECT count() AS c FROM numbers(2000) WHERE number %s (:ids) AND toString(number) %s (:names)";
            final Map<String, Object> params = Map.of("ids", List.of(4, 12, 989L), "names", List.of("4", "12", "it's \\ \t"));
            assertThat(count(clackShack, query.formatted("IN", "IN"), params)).isEqualTo(2);
            assertThat(count(clackShack, query.formatted("NOT IN", "NOT IN"), params)).isEqualTo(1997);
        }
    }

    @Test
    public void testInAndNotInWithEmptyList()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final Map<String, Object> params = Map.of("ids", List.of());
            assertThat(count(clackShack, "SELECT count() AS c FROM numbers(10) WHERE number IN (:ids)", params)).isZero();
            assertThat(count(clackShack, "SELECT count() AS c FROM numbers(10) WHERE number NOT IN (:ids)", params)).isEqualTo(10);
        }
    }

    @Test
    public void testExplicitStringArrayAndMapParams()
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final ResultSet result = clackShack.query("SELECT length(:list) AS l, :map['it''s'] AS v", List.of(
                    QueryParam.of("list", List.of("a", "it's"), DataTypes.STRING),
                    QueryParam.of("map", Map.of("it's", 7), DataTypes.STRING, DataTypes.INT_32)
            ));
            assertThat(result.getRow(0).get("l", BigInteger.class).longValue()).isEqualTo(2);
            assertThat(result.getRow(0).get("v", Integer.class)).isEqualTo(7);
        }
    }

    private static long count(final ClackShack clackShack, final String query, final Map<String, Object> params)
    {
        return clackShack.query(query, params).getRow(0).get("c", BigInteger.class).longValue();
    }
}
