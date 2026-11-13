package com.ethlo.clackshack.util;

/*-
 * #%L
 * ClackShack
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ethlo.clackshack.model.DataTypes;
import com.ethlo.clackshack.model.QueryParam;

public class QueryParamsTest
{
    @Test
    void asList()
    {
        // Given
        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("foo", "bar");
        input.put("fee", 1L);

        // When
        final List<QueryParam> params = QueryParams.asList(input);

        // Then
        assertThat(params).hasSize(2);
        assertThat(params.get(0).getType()).isEqualTo("String");
        assertThat(params.get(1).getType()).isEqualTo("Int64");
    }

    @Test
    void collectionOfIntegersIsArray()
    {
        final QueryParam param = QueryParams.of("ids", List.of(4, 12, 989));
        assertThat(param.getType()).isEqualTo("Array(Int32)");
        assertThat(param.getValue()).isEqualTo("[4,12,989]");
    }

    @Test
    void stringsAreQuotedAndEscaped()
    {
        final QueryParam param = QueryParams.of("names", Set.of("it's \\ ok"));
        assertThat(param.getType()).isEqualTo("Array(String)");
        assertThat(param.getValue()).isEqualTo("['it\\'s \\\\ ok']");
    }

    @Test
    void objectAndPrimitiveArraysAreArrays()
    {
        assertThat(QueryParams.of("a", new String[]{"a", "b"}).getValue()).isEqualTo("['a','b']");
        final QueryParam primitive = QueryParams.of("a", new long[]{1, 2});
        assertThat(primitive.getType()).isEqualTo("Array(Int64)");
        assertThat(primitive.getValue()).isEqualTo("[1,2]");
    }

    @Test
    void numericElementTypesAreWidened()
    {
        assertThat(QueryParams.of("a", List.of(1, 2L)).getType()).isEqualTo("Array(Int64)");
        assertThat(QueryParams.of("a", List.of(1, 2.5)).getType()).isEqualTo("Array(Float64)");
        assertThat(QueryParams.of("a", List.of(1.5f, 2.5f)).getType()).isEqualTo("Array(Float32)");
        assertThat(QueryParams.of("a", List.of(0.1f, 1.0d)).getValue()).isEqualTo("[" + (double) 0.1f + ",1.0]");
    }

    @Test
    void emptyCollectionIsArrayOfNothing()
    {
        final QueryParam param = QueryParams.of("a", List.of());
        assertThat(param.getType()).isEqualTo("Array(Nothing)");
        assertThat(param.getValue()).isEqualTo("[]");
    }

    @Test
    void nestedCollections()
    {
        final QueryParam param = QueryParams.of("a", List.of(List.of("x"), List.of("y", "z")));
        assertThat(param.getType()).isEqualTo("Array(Array(String))");
        assertThat(param.getValue()).isEqualTo("[['x'],['y','z']]");
        assertThat(QueryParams.of("a", List.of(List.of(1), List.of(2L))).getType()).isEqualTo("Array(Array(Int64))");
        assertThat(QueryParams.of("a", List.of(List.of(), List.of("x"))).getType()).isEqualTo("Array(Array(String))");
    }

    @Test
    void uninferableElementsAreRejected()
    {
        assertThatThrownBy(() -> QueryParams.of("a", Arrays.asList("x", null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");
        assertThatThrownBy(() -> QueryParams.of("a", List.of("x", 1))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("String and Int32");
        assertThatThrownBy(() -> QueryParams.of("a", List.of(BigDecimal.ONE))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("BigDecimal");
        assertThatThrownBy(() -> QueryParams.of("a", List.of(Map.of("k", 1)))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Map");
        assertThatThrownBy(() -> QueryParams.of("a", List.of(1L, 1.5))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Int64 and Float64");
    }

    @Test
    void inetAddressIsSentWithoutHostname() throws Exception
    {
        final InetAddress address = InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1});
        assertThat(QueryParam.of("ips", List.of(address), DataTypes.IP_V4).getValue()).isEqualTo("['127.0.0.1']");
    }
}
