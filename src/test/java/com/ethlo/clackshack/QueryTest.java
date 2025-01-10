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
}
