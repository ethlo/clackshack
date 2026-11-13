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
import java.net.InetAddress;
import java.time.LocalDateTime;
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
            assertThat(count(clackShack, "SELECT has(:names, 'it\\'s \\\\ \\t') AS c", params)).isEqualTo(1);
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
    public void testExplicitStringArrayAndMapParams() throws Exception
    {
        try (final ClackShack clackShack = new ClackShackImpl(baseUrl))
        {
            final ResultSet result = clackShack.query("SELECT length(:list) AS l, :map['it''s'] AS v", List.of(
                    QueryParam.of("list", List.of("a", "it's"), DataTypes.STRING),
                    QueryParam.of("map", Map.of("it's", 7), DataTypes.STRING, DataTypes.INT_32)
            ));
            assertThat(result.getRow(0).get("l", BigInteger.class).longValue()).isEqualTo(2);
            assertThat(result.getRow(0).get("v", Integer.class)).isEqualTo(7);

            final Map<String, Object> floats = Map.of("f", List.of(0.1f, 1.0d));
            assertThat(count(clackShack, "SELECT count() AS c FROM numbers(1) WHERE toFloat64(toFloat32(0.1)) IN (:f)", floats)).isEqualTo(1);
            final List<QueryParam> ips = List.of(QueryParam.of("ips", List.of(InetAddress.getLoopbackAddress()), DataTypes.IP_V4));
            final List<QueryParam> times = List.of(QueryParam.of("t", List.of(LocalDateTime.parse("2026-01-02T03:04:05.123")), "DateTime64(3)"));
            assertThat(clackShack.query("SELECT has(:t, toDateTime64('2026-01-02 03:04:05.123', 3)) AS c", times).getRow(0).get("c", Short.class)).isEqualTo((short) 1);
            assertThat(clackShack.query("SELECT has(:ips, toIPv4('127.0.0.1')) AS c", ips).getRow(0).get("c", Short.class)).isEqualTo((short) 1);
        }
    }

    private static long count(final ClackShack clackShack, final String query, final Map<String, Object> params)
    {
        return ((Number) clackShack.query(query, params).getRow(0).get("c")).longValue();
    }
}
